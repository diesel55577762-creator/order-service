package com.example.order_service.service;

import com.example.order_service.client.InventoryClient;
import com.example.order_service.client.ProductServiceClient;
import com.example.order_service.model.dto.OrderItemDto;
import com.example.order_service.model.dto.ProductDto;
import com.example.order_service.model.dto.ProductInfoRequest;
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
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductServiceClient productServiceClient;
    private final InventoryClient inventoryClient;


    public OrderDto create(OrderDto dto, UUID warehouseId){
        List<UUID> items = dto.getOrderItems().stream()
                .map(OrderItemDto::getOrderId)
                .toList();
        ProductInfoRequest productInfoRequest = ProductInfoRequest.builder()
                .ids(items)
                .build();

        List<ProductDto> products = productServiceClient.getProductInfoRequest(productInfoRequest);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ProductDto product : products) {
            totalAmount = totalAmount.add(product.getPrice());
        }
        for (OrderItemDto item : dto.getOrderItems()) {
            try {
                inventoryClient.reserveProduct(item.getProductId(), warehouseId, item.getQuantity());
            } catch (FeignException e) {
                throw new RuntimeException("Недостаточно товара на складе для продукта: " + item.getProductId());
            }
        }
        Order order = Order.builder()
                .status(Status.NEW)
                .totalAmount(totalAmount)
                .created_At(Instant.now())
                .build();
        orderRepository.save(order);


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
