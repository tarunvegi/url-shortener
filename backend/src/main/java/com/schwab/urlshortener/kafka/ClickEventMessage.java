package com.schwab.urlshortener.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClickEventMessage {
    private String shortCode;
    private String ipAddress;
    private String referrer;
    private String userAgent;
    private LocalDateTime clickedAt;
}
