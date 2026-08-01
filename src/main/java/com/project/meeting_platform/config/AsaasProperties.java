package com.project.meeting_platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "asaas")
public record AsaasProperties(
        String apiKey,
        String baseUrl,
        String webhookToken
) {
    public boolean isApiConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public boolean hasWebhookToken() {
        return webhookToken != null && !webhookToken.isBlank();
    }
}
