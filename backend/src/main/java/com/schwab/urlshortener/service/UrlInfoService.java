package com.schwab.urlshortener.service;

import com.schwab.urlshortener.api.dto.UrlInfoResponse;
import com.schwab.urlshortener.api.exception.UrlNotFoundException;
import com.schwab.urlshortener.domain.model.ShortUrl;
import com.schwab.urlshortener.domain.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UrlInfoService {

    private final ShortUrlRepository shortUrlRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    public UrlInfoResponse getByCode(String code) {
        ShortUrl shortUrl = shortUrlRepository.findByCode(code)
                .orElseThrow(() -> new UrlNotFoundException(code));
        return toResponse(shortUrl);
    }

    public List<UrlInfoResponse> getAll() {
        return shortUrlRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private UrlInfoResponse toResponse(ShortUrl shortUrl) {
        return UrlInfoResponse.builder()
                .code(shortUrl.getCode())
                .shortUrl(baseUrl + "/" + shortUrl.getCode())
                .originalUrl(shortUrl.getOriginalUrl())
                .customAlias(shortUrl.getCustomAlias())
                .createdAt(shortUrl.getCreatedAt())
                .expiresAt(shortUrl.getExpiresAt())
                .active(!shortUrl.isExpired())
                .build();
    }
}
