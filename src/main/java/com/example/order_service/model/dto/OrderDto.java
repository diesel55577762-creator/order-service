package com.example.order_service.model.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class OrderDto {
    private UUID id;
    private UUID customerId;
    private String status;
    private BigDecimal totalAmount;
    private Instant createdAt;
}