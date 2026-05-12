package com.example.order_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import com.example.order_service.model.dto.CustomerDto;
import org.springframework.web.bind.annotation.*;
import com.example.order_service.service.CustomerService;

import java.util.UUID;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary ="Запрос на создание Сервиса")
    @PostMapping
    public CustomerDto create(@RequestBody CustomerDto dto) {
        return customerService.create(dto);
    }


    @Operation(summary = "Запрос на получение айди")
    @GetMapping("/{id}")

    public CustomerDto getById(@PathVariable UUID id) {
        return customerService.getById(id);
    }
}