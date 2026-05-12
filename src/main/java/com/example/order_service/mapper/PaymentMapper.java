package com.example.order_service.mapper;

import com.example.order_service.model.dto.PaymentDto;
import com.example.order_service.model.entity.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentDto toDto(Payment payment);

    Payment toEntity(PaymentDto paymentDto);
}