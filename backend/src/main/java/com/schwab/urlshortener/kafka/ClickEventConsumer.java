package com.schwab.urlshortener.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.schwab.urlshortener.domain.model.ClickEvent;
import com.schwab.urlshortener.domain.repository.ClickEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ClickEventConsumer {

    private final ClickEventRepository clickEventRepository;
    private final ObjectMapper objectMapper;

    public ClickEventConsumer(ClickEventRepository clickEventRepository) {
        this.clickEventRepository = clickEventRepository;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @KafkaListener(topics = "click-events", groupId = "url-shortener-group")
    public void consume(String message) {
        try {
            ClickEventMessage event = objectMapper.readValue(message, ClickEventMessage.class);
            ClickEvent clickEvent = ClickEvent.builder()
                    .shortCode(event.getShortCode())
                    .clickedAt(event.getClickedAt())
                    .ipAddress(event.getIpAddress())
                    .referrer(event.getReferrer())
                    .userAgent(event.getUserAgent())
                    .build();
            clickEventRepository.save(clickEvent);
        } catch (Exception e) {
            log.error("Failed to process click event: {}", e.getMessage());
        }
    }
}
