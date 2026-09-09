package com.example.order_service.model.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class OrderCreatedEvent {
    private UUID orderId;
    private List<OrderItemDto> orderItems;
}
