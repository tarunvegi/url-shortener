package com.schwab.urlshortener.service;

import com.schwab.urlshortener.domain.model.ShortUrl;
import com.schwab.urlshortener.domain.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpiredUrlCleanupService {

    private final ShortUrlRepository shortUrlRepository;

    @Scheduled(cron = "0 0 2 * * *") // runs daily at 2am
    public void deleteExpiredUrls() {
        List<ShortUrl> expired = shortUrlRepository.findByExpiresAtBeforeAndExpiresAtIsNotNull(LocalDateTime.now());
        if (!expired.isEmpty()) {
            shortUrlRepository.deleteAll(expired);
            log.info("Cleaned up {} expired URLs", expired.size());
        }
    }
}
