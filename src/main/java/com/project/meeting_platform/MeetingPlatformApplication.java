package com.project.meeting_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MeetingPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeetingPlatformApplication.class, args);
	}

}
