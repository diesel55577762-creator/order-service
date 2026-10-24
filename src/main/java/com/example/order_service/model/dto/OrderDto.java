package com.example.order_service.model.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
@Builder
@Data
public class OrderDto {
    private UUID id;
    private UUID customerId;
    private String status;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private List<OrderItemDto> orderItems;

}