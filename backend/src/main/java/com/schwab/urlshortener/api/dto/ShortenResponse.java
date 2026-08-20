package com.schwab.urlshortener.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ShortenResponse {
    private String shortUrl;
    private String code;
    private String originalUrl;
    private LocalDateTime expiresAt;
}
