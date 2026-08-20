package com.schwab.urlshortener.config;

import com.schwab.urlshortener.api.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitConfig {

    @Value("${app.rate-limit.capacity}")
    private long capacity;

    @Value("${app.rate-limit.refill-per-minute}")
    private long refillPerMinute;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public void checkRateLimit(String clientIp) {
        Bucket bucket = buckets.computeIfAbsent(clientIp, this::newBucket);
        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException("Rate limit exceeded. Max " + refillPerMinute + " requests per minute.");
        }
    }

    private Bucket newBucket(String ip) {
        Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(refillPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
