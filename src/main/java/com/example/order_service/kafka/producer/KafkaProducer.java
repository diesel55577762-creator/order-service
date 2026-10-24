package com.example.order_service.kafka.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Component
public class KafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendEvent(String topicName, String key, Object value){
        String json = objectMapper.writeValueAsString(value);
        kafkaTemplate.send(topicName, key, json);
    }
}
