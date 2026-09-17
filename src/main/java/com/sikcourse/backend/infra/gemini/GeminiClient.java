package com.sikcourse.backend.infra.gemini;

import com.sikcourse.backend.global.config.GeminiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Component
public class GeminiClient {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 500;
    private static final Pattern GEMINI_KEY_QUERY_PARAM = Pattern.compile("([?&]key=)[^&\\s]+");

    private final GeminiProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GeminiClient(GeminiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.connectTimeoutMillis()))
                .build();
        if (!properties.enabled()) {
            log.info("Gemini API is disabled because GEMINI_API_KEY is not configured.");
        }
    }

    public Optional<String> generate(String prompt) {
        if (!properties.enabled()) {
            return Optional.empty();
        }

        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "contents", List.of(Map.of(
                            "parts", List.of(Map.of("text", prompt))
                    )),
                    "generationConfig", Map.of(
                            "temperature", 0.4,
                            "maxOutputTokens", 160
                    )
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(geminiUri())
                    .timeout(Duration.ofMillis(properties.readTimeoutMillis()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Gemini API failed with status: {}, error: {}",
                        response.statusCode(),
                        extractErrorMessage(response.body()));
                return Optional.empty();
            }

            return extractText(response.body());
        } catch (Exception exception) {
            log.warn("Gemini API call failed: {}: {}",
                    exception.getClass().getSimpleName(),
                    sanitizeErrorMessage(exception.getMessage()));
            return Optional.empty();
        }
    }

    private URI geminiUri() {
        String encodedModel = urlEncode(properties.model());
        String encodedKey = urlEncode(properties.apiKey());
        return URI.create(properties.baseUrl()
                + "/v1beta/models/"
                + encodedModel
                + ":generateContent?key="
                + encodedKey);
    }

    private Optional<String> extractText(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode textNode = root.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text");
        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            return Optional.empty();
        }
        return Optional.of(textNode.asText().trim());
    }

    private String extractErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "empty response body";
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode messageNode = root.path("error").path("message");
            if (!messageNode.isMissingNode() && !messageNode.asText().isBlank()) {
                return sanitizeErrorMessage(messageNode.asText());
            }
        } catch (Exception ignored) {
            // Fall through to logging a sanitized, truncated response body.
        }

        return sanitizeErrorMessage(responseBody);
    }

    private String sanitizeErrorMessage(String message) {
        if (message == null || message.isBlank()) {
            return "no error message";
        }

        String sanitized = GEMINI_KEY_QUERY_PARAM.matcher(message).replaceAll("$1<redacted>");
        sanitized = sanitized.replaceAll("\\s+", " ").trim();
        if (sanitized.length() > MAX_ERROR_MESSAGE_LENGTH) {
            return sanitized.substring(0, MAX_ERROR_MESSAGE_LENGTH) + "...";
        }
        return sanitized;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
