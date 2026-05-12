package com.example.order_service.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.order_service.mapper.CustomerMapper;
import com.example.order_service.model.dto.CustomerDto;
import com.example.order_service.model.entity.Customer;
import org.springframework.stereotype.Service;
import com.example.order_service.repository.CustomerRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerDto create(CustomerDto dto) {
        Customer customer = customerMapper.toEntity(dto);
        customer = customerRepository.save(customer);
        log.info("Клиент создан: id={}", customer.getId());
        return customerMapper.toDto(customer);
    }

    public CustomerDto getById(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Клиент не найден"));
        return customerMapper.toDto(customer);
    }
}