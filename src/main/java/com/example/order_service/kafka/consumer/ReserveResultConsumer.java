package com.example.order_service.kafka.consumer;

import com.example.order_service.model.dto.OrderReservedResult;
import com.example.order_service.model.enums.SagaStatus;
import com.example.order_service.service.OrderService;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReserveResultConsumer {
    private final OrderService orderService;
    private final ObjectMapper objectMapper;


    @KafkaListener(topics = "reserve_order_result", groupId = "groupEvent")
    public void consumeReserveResult(String message) {
        try {
            OrderReservedResult result = objectMapper.readValue(message, OrderReservedResult.class);
            UUID orderId = result.getOrderId();
            SagaStatus status = result.getStatus();
            log.info("Получен результат резервации: orderId={}, status={}", orderId, status);

            if (status == SagaStatus.RESERVED) {
                orderService.confirm(orderId);
            } else {
                orderService.cancel(orderId);
            }
        } catch (Exception e) {
            log.error("Ошибка обработки результата резервации: {}", e.getMessage());
        }
    }
}
