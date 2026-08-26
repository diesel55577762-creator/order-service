package com.example.order_service.scheduler;

import com.example.order_service.kafka.producer.KafkaProducer;
import com.example.order_service.model.enums.OutboxStatus;
import com.example.order_service.model.entity.OutboxEntity;
import com.example.order_service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxCreatedEventScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaProducer kafkaProducer;

    @Transactional
    @Scheduled(fixedDelay = 50000) // каждые 5 секунд
    public void publishPendingEvents() {
        List<OutboxEntity> pendingEvents = outboxEventRepository.findByStatus(OutboxStatus.NEW);
        if (pendingEvents.isEmpty()) {
            return;
        }
        log.info("Найдено {} событий для отправки", pendingEvents.size());

        for (OutboxEntity event : pendingEvents) {
            try {
                kafkaProducer.sendEvent("order-events", event.getAggregateId().toString(), event.getPayload());
                event.setStatus(OutboxStatus.SENT);
                outboxEventRepository.save(event);
                log.info("Событие {} отправлено в Kafka", event.getId());
            } catch (Exception e) {
                log.error("Ошибка при отправке события {}: {}", event.getId(), e.getMessage());
                event.setStatus(OutboxStatus.FAILED);
                outboxEventRepository.save(event);
            }
        }
    }
}


