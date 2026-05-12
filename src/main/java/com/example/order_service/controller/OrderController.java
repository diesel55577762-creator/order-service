package com.example.order_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import com.example.order_service.model.dto.OrderDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import com.example.order_service.service.OrderService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Создание сервиса заказов")
    @PostMapping
    public OrderDto create(@RequestBody OrderDto dto) {
        log.info("Отправлен запрос на создание сервиса заказов");
        return orderService.create(dto);
    }

    @Operation(summary = "получить заказ по id")
    @GetMapping("/{id}")
    public OrderDto getOrder(@PathVariable UUID id) {
        log.info("Отправлен запрос на получение заказа по id");
        return orderService.getOrder(id);
    }

    @Operation(summary = "Запрос на получение заказа")
    @GetMapping
    public List<OrderDto> getAll() {
        log.info("Отправлен запрос на получение списка заказов");
        return orderService.getAll();
    }

    @Operation(summary = "Запрос на подтверждение заказа")
    @PostMapping("/{id}/confirm")
    public void confirm(@PathVariable UUID id) {
        log.info("отправлен запрос на на подтверждение заказа");
        orderService.confirm(id);
    }

    @Operation(summary = "отмена заказа")
    @PostMapping("/{id}/cancel")
    public void cancel(@PathVariable UUID id) {
        log.info("Отправлен запрос на отмену заказа");
        orderService.cancel(id);
    }
}