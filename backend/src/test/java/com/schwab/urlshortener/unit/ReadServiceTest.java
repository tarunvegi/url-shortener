package com.schwab.urlshortener.unit;

import com.schwab.urlshortener.api.exception.UrlExpiredException;
import com.schwab.urlshortener.api.exception.UrlNotFoundException;
import com.schwab.urlshortener.domain.model.ShortUrl;
import com.schwab.urlshortener.domain.repository.ShortUrlRepository;
import com.schwab.urlshortener.kafka.ClickEventProducer;
import com.schwab.urlshortener.service.ReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ReadServiceTest {

    @Mock private ShortUrlRepository shortUrlRepository;
    @Mock private StringRedisTemplate redis;
    @Mock private ValueOperations<String, String> valueOps;
    @Mock private ClickEventProducer clickEventProducer;

    private ReadService readService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redis.opsForValue()).thenReturn(valueOps);
        readService = new ReadService(shortUrlRepository, redis, clickEventProducer);
        ReflectionTestUtils.setField(readService, "cacheTtlSeconds", 86400L);
    }

    @Test
    void resolvesFromCacheWhenPresent() {
        when(valueOps.get("url:abc123")).thenReturn("https://www.example.com");

        String url = readService.resolveAndTrack("abc123", "127.0.0.1", null, null);

        assertThat(url).isEqualTo("https://www.example.com");
        verifyNoInteractions(shortUrlRepository);
    }

    @Test
    void resolvesFromDatabaseOnCacheMiss() {
        when(valueOps.get("url:abc123")).thenReturn(null);
        ShortUrl shortUrl = ShortUrl.builder()
                .code("abc123")
                .originalUrl("https://www.example.com")
                .createdAt(LocalDateTime.now())
                .build();
        when(shortUrlRepository.findByCode("abc123")).thenReturn(Optional.of(shortUrl));

        String url = readService.resolveAndTrack("abc123", "127.0.0.1", null, null);

        assertThat(url).isEqualTo("https://www.example.com");
        verify(shortUrlRepository).findByCode("abc123");
    }

    @Test
    void throwsNotFoundForUnknownCode() {
        when(valueOps.get("url:unknown")).thenReturn(null);
        when(shortUrlRepository.findByCode("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> readService.resolveAndTrack("unknown", "127.0.0.1", null, null))
                .isInstanceOf(UrlNotFoundException.class);
    }

    @Test
    void throwsExpiredForExpiredUrl() {
        when(valueOps.get("url:old")).thenReturn(null);
        ShortUrl expired = ShortUrl.builder()
                .code("old")
                .originalUrl("https://www.example.com")
                .createdAt(LocalDateTime.now().minusDays(10))
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();
        when(shortUrlRepository.findByCode("old")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> readService.resolveAndTrack("old", "127.0.0.1", null, null))
                .isInstanceOf(UrlExpiredException.class);
    }
}
