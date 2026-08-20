package com.schwab.urlshortener.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ShortCodeGenerator {

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private final StringRedisTemplate redis;
    private final String counterKey;
    private final long batchSize;

    private long currentBatchStart = 0;
    private long currentBatchEnd = 0;

    public ShortCodeGenerator(
            StringRedisTemplate redis,
            @Value("${app.redis.counter-key}") String counterKey,
            @Value("${app.redis.counter-batch-size}") long batchSize) {
        this.redis = redis;
        this.counterKey = counterKey;
        this.batchSize = batchSize;
    }

    public synchronized String generate() {
        if (currentBatchStart >= currentBatchEnd) {
            Long newEnd = redis.opsForValue().increment(counterKey, batchSize);
            if (newEnd == null) throw new RuntimeException("Redis counter unavailable");
            currentBatchEnd = newEnd;
            currentBatchStart = currentBatchEnd - batchSize;
        }
        return toBase62(currentBatchStart++);
    }

    private String toBase62(long number) {
        if (number == 0) return "0";
        StringBuilder sb = new StringBuilder();
        while (number > 0) {
            sb.append(BASE62.charAt((int) (number % 62)));
            number /= 62;
        }
        return sb.reverse().toString();
    }
}
