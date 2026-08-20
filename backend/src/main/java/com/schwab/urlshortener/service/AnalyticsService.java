package com.schwab.urlshortener.service;

import com.schwab.urlshortener.api.dto.AnalyticsResponse;
import com.schwab.urlshortener.api.exception.UrlNotFoundException;
import com.schwab.urlshortener.domain.repository.ClickEventRepository;
import com.schwab.urlshortener.domain.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ClickEventRepository clickEventRepository;
    private final ShortUrlRepository shortUrlRepository;

    public AnalyticsResponse getAnalytics(String code) {
        if (!shortUrlRepository.existsByCode(code)) {
            throw new UrlNotFoundException(code);
        }

        long totalClicks = clickEventRepository.countByShortCode(code);

        // clicks per day for last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Object[]> dailyRaw = clickEventRepository.countClicksByDay(code, thirtyDaysAgo);
        Map<String, Long> clicksByDay = new LinkedHashMap<>();
        for (Object[] row : dailyRaw) {
            clicksByDay.put(row[0].toString(), (Long) row[1]);
        }

        // top 5 referrers
        List<Object[]> referrerRaw = clickEventRepository.topReferrers(code);
        Map<String, Long> topReferrers = new LinkedHashMap<>();
        referrerRaw.stream().limit(5).forEach(row ->
                topReferrers.put((String) row[0], (Long) row[1]));

        return AnalyticsResponse.builder()
                .code(code)
                .totalClicks(totalClicks)
                .clicksByDay(clicksByDay)
                .topReferrers(topReferrers)
                .build();
    }
}
