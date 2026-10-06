package com.devAssist.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openrouter")
public record OpenRouterConfig(
        String apiKey,
        String baseUrl,
        String model
) {
}