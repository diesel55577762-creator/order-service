package com.example.order_service.service;

import com.example.order_service.client.InventoryClient;
import com.example.order_service.client.ProductServiceClient;
import com.example.order_service.model.entity.Customer;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.model.dto.OrderDto;
import com.example.order_service.model.entity.Order;
import com.example.order_service.model.entity.Status;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.stereotype.Service;
import com.example.order_service.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableFeignClients(basePackages = "com.example.order service.client")
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductServiceClient productServiceClient;
    private final InventoryClient inventoryClient;

    public void processOrder(UUID productId, UUID warehouseId, int quantity, Customer customer) {
        ProductDto productDto = productServiceClient.getProduct(productId);
        BigDecimal price = productDto.getPrice();

        try {
            inventoryClient.reserveProduct(productId, warehouseId, quantity);
        } catch (FeignException e) {
            throw new RuntimeException("Недостаточно товара на складе");
        }
        BigDecimal totalCost = price.multiply(BigDecimal.valueOf(quantity));
        Order order = Order.builder()
                .customer(customer)
                .status(Status.NEW)
                .totalAmount(totalCost)
                .created_At(Instant.now())
                .build();

        orderRepository.save(order);


    }

    public OrderDto create(OrderDto dto){
        Order order = orderMapper.toEntity(dto);
        order = orderRepository.save(order);
        log.info("Заказ создан: id={}", order.getId());
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
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));

        order.setStatus(Status.CONFIRMED);
        orderRepository.save(order);
    }

    @Transactional
    public void cancel(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));

        order.setStatus(Status.CANCELLED);
        orderRepository.save(order);
    }
}
