package com.schwab.urlshortener.api.controller;

import com.schwab.urlshortener.api.dto.ShortenRequest;
import com.schwab.urlshortener.api.dto.ShortenResponse;
import com.schwab.urlshortener.api.dto.UrlInfoResponse;
import com.schwab.urlshortener.config.RateLimitConfig;
import com.schwab.urlshortener.service.UrlInfoService;
import com.schwab.urlshortener.service.WriteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/urls")
@RequiredArgsConstructor
public class UrlController {

    private final WriteService writeService;
    private final UrlInfoService urlInfoService;
    private final RateLimitConfig rateLimitConfig;

    @PostMapping
    public ResponseEntity<ShortenResponse> shorten(
            @Valid @RequestBody ShortenRequest request,
            HttpServletRequest httpRequest) {

        rateLimitConfig.checkRateLimit(getClientIp(httpRequest));
        ShortenResponse response = writeService.shorten(request, getClientIp(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<UrlInfoResponse>> listAll() {
        return ResponseEntity.ok(urlInfoService.getAll());
    }

    @GetMapping("/{code}")
    public ResponseEntity<UrlInfoResponse> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(urlInfoService.getByCode(code));
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        writeService.delete(code);
        return ResponseEntity.noContent().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
