package com.project.meeting_platform.auth.controller.User;

import com.project.meeting_platform.auth.dto.User.CurrentUserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me(
            Authentication authentication
    ) {
        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("");

        return ResponseEntity.ok(
                new CurrentUserResponse(authentication.getName(), role)
        );
    }
}