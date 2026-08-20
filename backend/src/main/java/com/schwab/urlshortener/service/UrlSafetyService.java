package com.schwab.urlshortener.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UrlSafetyService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Value("${anthropic.api-key:}")
    private String apiKey;

    public record SafetyResult(boolean safe, String reason) {}

    public SafetyResult check(String url) {
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("Anthropic API key not configured, skipping URL safety check");
            return new SafetyResult(true, "Safety check not configured");
        }

        try {
            String prompt = """
                    You are a URL safety checker. Analyze this URL and determine if it is potentially malicious, phishing, spam, or harmful.
                    Look for signs like: suspicious TLDs, phishing keywords (login, verify, account, suspended), brand impersonation, credential harvesting paths.

                    URL: %s

                    Reply with JSON only, no other text. Use false for safe if the URL looks suspicious:
                    {"safe": false, "reason": "explanation here"}
                    """.formatted(url);

            Map<String, Object> body = Map.of(
                    "model", "claude-haiku-4-5-20251001",
                    "max_tokens", 100,
                    "messages", List.of(Map.of("role", "user", "content", prompt))
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.anthropic.com/v1/messages"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Anthropic API returned {}, skipping safety check", response.statusCode());
                return new SafetyResult(true, "Safety check unavailable");
            }

            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("content").get(0).path("text").asText().trim();
            // strip markdown code fences if Claude wraps the response
            if (content.startsWith("```")) {
                content = content.replaceAll("^```[a-z]*\\n?", "").replaceAll("\\n?```$", "").trim();
            }
            JsonNode result = objectMapper.readTree(content);

            boolean safe = result.path("safe").asBoolean(true);
            String reason = result.path("reason").asText("No reason provided");

            log.info("Safety check for [{}]: safe={}, reason={}", url, safe, reason);
            return new SafetyResult(safe, reason);

        } catch (Exception e) {
            log.warn("Safety check failed for [{}]: {} — allowing URL", url, e.getMessage());
            return new SafetyResult(true, "Safety check failed, proceeding");
        }
    }
}
