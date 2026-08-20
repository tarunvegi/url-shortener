package com.schwab.urlshortener.service;

import com.schwab.urlshortener.api.dto.ShortenRequest;
import com.schwab.urlshortener.api.dto.ShortenResponse;
import com.schwab.urlshortener.api.exception.DuplicateAliasException;
import com.schwab.urlshortener.api.exception.UrlNotSafeException;
import com.schwab.urlshortener.domain.model.ShortUrl;
import com.schwab.urlshortener.domain.repository.ShortUrlRepository;
import com.schwab.urlshortener.service.UrlSafetyService.SafetyResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WriteService {

    private final ShortUrlRepository shortUrlRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final StringRedisTemplate redis;
    private final UrlSafetyService urlSafetyService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.redis.cache-ttl-seconds}")
    private long cacheTtlSeconds;

    public ShortenResponse shorten(ShortenRequest request, String clientIp) {
        SafetyResult safety = urlSafetyService.check(request.getOriginalUrl());
        if (!safety.safe()) {
            throw new UrlNotSafeException(safety.reason());
        }

        String code;

        if (request.getCustomAlias() != null && !request.getCustomAlias().isBlank()) {
            if (shortUrlRepository.existsByCustomAlias(request.getCustomAlias())) {
                throw new DuplicateAliasException("Alias '" + request.getCustomAlias() + "' is already taken");
            }
            code = request.getCustomAlias();
        } else {
            code = shortCodeGenerator.generate();
        }

        ShortUrl shortUrl = ShortUrl.builder()
                .code(code)
                .originalUrl(request.getOriginalUrl())
                .customAlias(request.getCustomAlias())
                .expiresAt(request.getExpiresAt())
                .createdByIp(clientIp)
                .build();

        shortUrlRepository.save(shortUrl);

        // cache it immediately so first redirect is fast
        redis.opsForValue().set("url:" + code, request.getOriginalUrl(), Duration.ofSeconds(cacheTtlSeconds));

        return ShortenResponse.builder()
                .shortUrl(baseUrl + "/" + code)
                .code(code)
                .originalUrl(request.getOriginalUrl())
                .expiresAt(request.getExpiresAt())
                .build();
    }

    public void delete(String code) {
        ShortUrl shortUrl = shortUrlRepository.findByCode(code)
                .orElseThrow(() -> new com.schwab.urlshortener.api.exception.UrlNotFoundException(code));
        shortUrlRepository.delete(shortUrl);
        redis.delete("url:" + code);
    }
}
