package com.project.meeting_platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "prospecting.openai")
public record OpenAiProperties(
        String apiKey,
        String baseUrl,
        String model
) {
}
