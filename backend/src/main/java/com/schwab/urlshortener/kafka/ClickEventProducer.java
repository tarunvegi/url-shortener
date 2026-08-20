package com.schwab.urlshortener.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class ClickEventProducer {

    private static final String TOPIC = "click-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ClickEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public void publish(String shortCode, String ip, String referrer, String userAgent) {
        try {
            ClickEventMessage message = new ClickEventMessage(shortCode, ip, referrer, userAgent, LocalDateTime.now());
            String json = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(TOPIC, shortCode, json);
        } catch (Exception e) {
            // log but don't fail the redirect - analytics loss is acceptable
            log.warn("Failed to publish click event for code={}: {}", shortCode, e.getMessage());
        }
    }
}
