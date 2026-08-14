package com.project.meeting_platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "prospecting")
public record ProspectingProperties(
        int maxCompanies
) {
}
