package com.example.order_service.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.model.dto.OrderDto;
import com.example.order_service.model.entity.Order;
import com.example.order_service.model.entity.Status;
import org.springframework.stereotype.Service;
import com.example.order_service.repository.OrderRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

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
