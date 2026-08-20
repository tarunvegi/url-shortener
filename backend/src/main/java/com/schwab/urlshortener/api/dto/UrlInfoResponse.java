package com.schwab.urlshortener.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UrlInfoResponse {
    private String code;
    private String shortUrl;
    private String originalUrl;
    private String customAlias;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean active;
}
