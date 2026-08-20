package com.schwab.urlshortener.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AnalyticsResponse {
    private String code;
    private long totalClicks;
    private Map<String, Long> clicksByDay;
    private Map<String, Long> topReferrers;
}
