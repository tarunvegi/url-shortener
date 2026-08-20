package com.schwab.urlshortener.service;

import com.schwab.urlshortener.api.exception.UrlExpiredException;
import com.schwab.urlshortener.api.exception.UrlNotFoundException;
import com.schwab.urlshortener.domain.model.ShortUrl;
import com.schwab.urlshortener.domain.repository.ShortUrlRepository;
import com.schwab.urlshortener.kafka.ClickEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReadService {

    private final ShortUrlRepository shortUrlRepository;
    private final StringRedisTemplate redis;
    private final ClickEventProducer clickEventProducer;

    @Value("${app.redis.cache-ttl-seconds}")
    private long cacheTtlSeconds;

    public String resolveAndTrack(String code, String ip, String referrer, String userAgent) {
        String cacheKey = "url:" + code;

        // check cache first
        String cachedUrl = redis.opsForValue().get(cacheKey);
        if (cachedUrl != null) {
            clickEventProducer.publish(code, ip, referrer, userAgent);
            return cachedUrl;
        }

        // cache miss - hit database
        ShortUrl shortUrl = shortUrlRepository.findByCode(code)
                .orElseThrow(() -> new UrlNotFoundException(code));

        if (shortUrl.isExpired()) {
            throw new UrlExpiredException(code);
        }

        // populate cache
        redis.opsForValue().set(cacheKey, shortUrl.getOriginalUrl(), Duration.ofSeconds(cacheTtlSeconds));

        clickEventProducer.publish(code, ip, referrer, userAgent);

        return shortUrl.getOriginalUrl();
    }
}
