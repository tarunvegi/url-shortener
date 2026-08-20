package com.schwab.urlshortener.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schwab.urlshortener.api.dto.ShortenRequest;
import com.schwab.urlshortener.kafka.ClickEventProducer;
import com.schwab.urlshortener.service.ShortCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UrlControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private StringRedisTemplate redisTemplate;
    @MockBean private KafkaTemplate<String, String> kafkaTemplate;
    @MockBean private ShortCodeGenerator shortCodeGenerator;
    @MockBean private ValueOperations<String, String> valueOperations;

    private final AtomicLong counter = new AtomicLong(1);

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(shortCodeGenerator.generate()).thenAnswer(inv -> "code" + counter.getAndIncrement());
    }

    @Test
    void shortensUrlAndReturns201() throws Exception {
        ShortenRequest request = new ShortenRequest();
        request.setOriginalUrl("https://www.example.com/some/long/path");

        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortUrl").exists())
                .andExpect(jsonPath("$.code").isNotEmpty());
    }

    @Test
    void returns400ForInvalidUrl() throws Exception {
        ShortenRequest request = new ShortenRequest();
        request.setOriginalUrl("not-a-valid-url");

        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns409ForDuplicateAlias() throws Exception {
        ShortenRequest first = new ShortenRequest();
        first.setOriginalUrl("https://www.example.com");
        first.setCustomAlias("my-alias");

        mockMvc.perform(post("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        ShortenRequest second = new ShortenRequest();
        second.setOriginalUrl("https://www.other.com");
        second.setCustomAlias("my-alias");

        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isConflict());
    }

    @Test
    void listAllReturnsArray() throws Exception {
        mockMvc.perform(get("/api/v1/urls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
