package com.sikcourse.backend.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gemini")
public record GeminiProperties(
        String apiKey,
        String model,
        String baseUrl,
        int connectTimeoutMillis,
        int readTimeoutMillis
) {

    public boolean enabled() {
        return apiKey != null && !apiKey.isBlank();
    }
}
