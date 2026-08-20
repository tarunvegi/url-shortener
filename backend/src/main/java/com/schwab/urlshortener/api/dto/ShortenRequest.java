package com.schwab.urlshortener.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

@Data
public class ShortenRequest {

    @NotBlank(message = "URL is required")
    @URL(message = "Must be a valid URL")
    private String originalUrl;

    @Size(max = 50, message = "Custom alias cannot exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]*$", message = "Alias can only contain letters, numbers, hyphens, and underscores")
    private String customAlias;

    private LocalDateTime expiresAt;
}
