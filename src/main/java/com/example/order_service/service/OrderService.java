package com.example.order_service.service;

import com.example.order_service.client.InventoryClient;
import com.example.order_service.client.ProductServiceClient;
import com.example.order_service.kafka.producer.KafkaProducer;
import com.example.order_service.model.dto.*;
import com.example.order_service.model.entity.Customer;
import com.example.order_service.model.entity.OutboxEntity;
import com.example.order_service.model.enums.*;
import com.example.order_service.repository.CustomerRepository;
import com.example.order_service.repository.OutboxEventRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.model.entity.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.example.order_service.repository.OrderRepository;

import javax.naming.ServiceUnavailableException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductServiceClient productServiceClient;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProducer kafkaProducer;
    private final CustomerRepository customerRepository;
    private final OutboxEventRepository outboxEventRepository;



    public OrderDto createFallback(OrderDto dto, Throwable ex) throws ServiceUnavailableException {
        throw new ServiceUnavailableException("Сервис временно недоступен. Попробуйте позже");
    }

    @Retry(name = "orderCreateRetry", fallbackMethod = "createFallback")
    @CircuitBreaker(name = "orderCreateCB", fallbackMethod = "createFallback")
    @RateLimiter(name = "orderCreateRL")
    @Bulkhead(name = "orderCreateBH")
    public OrderDto create(OrderDto dto) {
        List<UUID> items = dto.getOrderItems()
                .stream()
                .map(OrderItemDto::getProductId)
                .toList();
        ProductInfoRequest productInfoRequest = ProductInfoRequest.builder()
                .ids(items)
                .build();
        List<ProductDto> products = productServiceClient.getProductInfoRequest(productInfoRequest);
        for (OrderItemDto item : dto.getOrderItems()) {
            StatusReservedItem status = inventoryClient.reserveProduct(
                    item.getProductId(),
                    item.getQuantity()
            );

            if (status != StatusReservedItem.SUCCESS) {
                log.error("Резервация не удалась для продукта: {}, статус: {}",
                        item.getProductId(), status);
                throw new RuntimeException(
                        "Недостаточно товара на складе для продукта: " + item.getProductId()
                );
            }
        }
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemDto item : dto.getOrderItems()) {
            for (ProductDto product : products) {
                if (product.getId().equals(item.getProductId())) {
                    totalAmount = totalAmount.add(
                            product.getPrice()
                                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                    );
                }
            }
        }
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Клиент не найден"));
        Order order = Order.builder()
                .customer(customer)
                .status(Status.NEW)
                .totalAmount(totalAmount)
                .created_At(Instant.now())
                .build();
        Order saved = orderRepository.save(order);

        OutboxEntity outbox = OutboxEntity.builder()
                .id(UUID.randomUUID())
                .aggregateId(saved.getId())
                .aggregateType(AggregateType.ORDER)
                .eventType(EventType.ORDER_CREATED)
                .status(OutboxStatus.NEW)
                .createdAt(Instant.now())
                .build();
        outboxEventRepository.save(outbox);

        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(saved.getId(), dto.getOrderItems());
        kafkaProducer.sendEvent("created_order_event", order.getId().toString(), orderCreatedEvent);

        return orderMapper.toDto(order);


    }

    public OrderDto getOrder(UUID id){
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));
        return orderMapper.toDto(order);
    }

    public List<OrderDto> getAll() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Transactional
    public void confirm(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));

        order.setStatus(Status.CONFIRMED);
        orderRepository.save(order);

        OrderConfirmedEvent event = new OrderConfirmedEvent(order.getId());
        OutboxEntity outbox = OutboxEntity.builder()
                .id(UUID.randomUUID())
                .aggregateId(order.getId())
                .aggregateType(AggregateType.ORDER)
                .eventType(EventType.ORDER_CONFIRMED)
                .status(OutboxStatus.NEW)
                .build();
        outboxEventRepository.save(outbox);

        log.info("Заказ подтверждён, событие сохранено в outbox: {}", id);

    }

    @Transactional
    public void cancel(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));

        order.setStatus(Status.CANCELLED);
        orderRepository.save(order);
        OrderCancelledEvent event = new OrderCancelledEvent(order.getId());

        OutboxEntity outbox = OutboxEntity.builder()
                .id(UUID.randomUUID())
                .aggregateId(order.getId())
                .aggregateType(AggregateType.ORDER)
                .eventType(EventType.ORDER_CANCELLED)
                .status(OutboxStatus.NEW)
                .createdAt(Instant.now())
                .build();
        outboxEventRepository.save(outbox);

        log.info("Заказ отменён, событие сохранено в outbox: {}", id);
    }
}
