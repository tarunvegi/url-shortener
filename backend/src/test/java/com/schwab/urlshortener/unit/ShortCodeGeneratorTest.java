package com.schwab.urlshortener.unit;

import com.schwab.urlshortener.service.ShortCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class ShortCodeGeneratorTest {

    @Mock
    private StringRedisTemplate redis;
    @Mock
    private ValueOperations<String, String> valueOps;

    private ShortCodeGenerator generator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(anyString(), anyLong())).thenReturn(1000L, 2000L, 3000L);
        generator = new ShortCodeGenerator(redis, "url:counter:test", 1000);
    }

    @Test
    void generatesNonNullCode() {
        String code = generator.generate();
        assertThat(code).isNotNull().isNotBlank();
    }

    @Test
    void generatesUniqueCodesWithinBatch() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            codes.add(generator.generate());
        }
        assertThat(codes).hasSize(100);
    }

    @Test
    void generatedCodeIsBase62() {
        String code = generator.generate();
        assertThat(code).matches("[0-9A-Za-z]+");
    }
}
