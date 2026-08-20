package com.schwab.urlshortener.api.controller;

import com.schwab.urlshortener.api.dto.AnalyticsResponse;
import com.schwab.urlshortener.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/{code}")
    public ResponseEntity<AnalyticsResponse> getAnalytics(@PathVariable String code) {
        return ResponseEntity.ok(analyticsService.getAnalytics(code));
    }
}
