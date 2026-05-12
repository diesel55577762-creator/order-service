package com.example.order_service.mapper;

import com.example.order_service.model.dto.CustomerDto;
import com.example.order_service.model.entity.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerDto toDto(Customer customer);

    Customer toEntity(CustomerDto customerDto);
}