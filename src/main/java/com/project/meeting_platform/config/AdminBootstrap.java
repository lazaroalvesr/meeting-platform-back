package com.project.meeting_platform.config;

import com.project.meeting_platform.auth.Service.Auth.AuthService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private final AuthService authService;
    private final Environment environment;

    public AdminBootstrap(AuthService authService, Environment environment) {
        this.authService = authService;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (authService.hasRegisteredUsers()) {
            return;
        }

        String name = environment.getRequiredProperty("APP_ADMIN_NAME");
        String email = environment.getRequiredProperty("APP_ADMIN_EMAIL");
        String password = environment.getRequiredProperty("APP_ADMIN_PASSWORD");

        authService.createAdmin(name, email, password);
    }

}
