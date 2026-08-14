package com.project.meeting_platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "prospecting.google")
public record GooglePlacesProperties(
        String placesApiKey,
        String baseUrl
) {
}
