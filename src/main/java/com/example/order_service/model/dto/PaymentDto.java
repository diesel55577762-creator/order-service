package com.example.order_service.model.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class PaymentDto {
    private UUID id;
    private UUID orderId;
    private BigDecimal amount;
    private String status;
    private Instant createdAt;
}