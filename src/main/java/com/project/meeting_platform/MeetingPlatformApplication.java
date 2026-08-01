package com.project.meeting_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.project.meeting_platform.config.AsaasProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AsaasProperties.class)
public class MeetingPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeetingPlatformApplication.class, args);
	}

}
