package com.example.order_service.mapper;

import com.example.order_service.model.dto.OrderDto;
import com.example.order_service.model.entity.Customer;
import com.example.order_service.model.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "created_At", target = "createdAt")
    OrderDto toDto(Order order);

    @Mapping(source = "customerId", target = "customer")
    Order toEntity(OrderDto orderDto);

    default Customer map(UUID customerId) {
        if (customerId == null) {
            return null;
        }

        Customer customer = new Customer();
        customer.setId(customerId);
        return customer;
    }
}
