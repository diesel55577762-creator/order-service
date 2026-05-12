package com.example.order_service.mapper;

import com.example.order_service.model.dto.OrderItemDto;
import com.example.order_service.model.entity.OrderItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    OrderItemDto toDto(OrderItem orderItem);

    OrderItem toEntity(OrderItemDto orderItemDto);
}