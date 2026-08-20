package com.schwab.urlshortener.unit;

import com.schwab.urlshortener.api.dto.ShortenRequest;
import com.schwab.urlshortener.api.dto.ShortenResponse;
import com.schwab.urlshortener.api.exception.DuplicateAliasException;
import com.schwab.urlshortener.domain.model.ShortUrl;
import com.schwab.urlshortener.domain.repository.ShortUrlRepository;
import com.schwab.urlshortener.service.ShortCodeGenerator;
import com.schwab.urlshortener.service.UrlSafetyService;
import com.schwab.urlshortener.service.UrlSafetyService.SafetyResult;
import com.schwab.urlshortener.service.WriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WriteServiceTest {

    @Mock private ShortUrlRepository shortUrlRepository;
    @Mock private ShortCodeGenerator shortCodeGenerator;
    @Mock private StringRedisTemplate redis;
    @Mock private ValueOperations<String, String> valueOps;
    @Mock private UrlSafetyService urlSafetyService;

    private WriteService writeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redis.opsForValue()).thenReturn(valueOps);
        when(urlSafetyService.check(any())).thenReturn(new SafetyResult(true, "safe"));
        writeService = new WriteService(shortUrlRepository, shortCodeGenerator, redis, urlSafetyService);
        ReflectionTestUtils.setField(writeService, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(writeService, "cacheTtlSeconds", 86400L);
    }

    @Test
    void shortensUrlSuccessfully() {
        when(shortCodeGenerator.generate()).thenReturn("abc123");
        when(shortUrlRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ShortenRequest request = new ShortenRequest();
        request.setOriginalUrl("https://www.example.com/some/long/url");

        ShortenResponse response = writeService.shorten(request, "127.0.0.1");

        assertThat(response.getShortUrl()).isEqualTo("http://localhost:8080/abc123");
        assertThat(response.getCode()).isEqualTo("abc123");
        verify(shortUrlRepository).save(any(ShortUrl.class));
    }

    @Test
    void usesCustomAliasWhenProvided() {
        when(shortUrlRepository.existsByCustomAlias("my-link")).thenReturn(false);
        when(shortUrlRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ShortenRequest request = new ShortenRequest();
        request.setOriginalUrl("https://www.example.com");
        request.setCustomAlias("my-link");

        ShortenResponse response = writeService.shorten(request, "127.0.0.1");

        assertThat(response.getCode()).isEqualTo("my-link");
        verify(shortCodeGenerator, never()).generate();
    }

    @Test
    void throwsExceptionForDuplicateAlias() {
        when(shortUrlRepository.existsByCustomAlias("taken")).thenReturn(true);

        ShortenRequest request = new ShortenRequest();
        request.setOriginalUrl("https://www.example.com");
        request.setCustomAlias("taken");

        assertThatThrownBy(() -> writeService.shorten(request, "127.0.0.1"))
                .isInstanceOf(DuplicateAliasException.class);
    }
}
