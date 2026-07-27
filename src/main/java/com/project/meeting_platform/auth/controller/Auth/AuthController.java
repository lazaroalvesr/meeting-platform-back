package com.project.meeting_platform.auth.controller.Auth;

import com.project.meeting_platform.Model.User;
import com.project.meeting_platform.auth.Service.Auth.AuthService;
import com.project.meeting_platform.auth.Service.Auth.JwtService;
import com.project.meeting_platform.auth.Service.RefreshToken.RefreshTokenService;
import com.project.meeting_platform.auth.dto.User.LoginRequest;
import com.project.meeting_platform.auth.dto.User.LoginResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final long refreshTokenExpirationDays;
    private final boolean secureCookie;

    public AuthController(
            AuthService authService,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            @Value("${jwt.refresh-token-expiration-days}") long refreshTokenExpirationDays,
            @Value("${app.cookie.secure}") boolean secureCookie
    ) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
        this.secureCookie = secureCookie;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        User user = authService.authenticate(request);

        String accessToken = jwtService.generateAcessToken(user);
        String refreshToken = refreshTokenService.create(user);

        addRefreshTokenCookie(response, refreshToken);

        return ResponseEntity.ok(
                new LoginResponse(accessToken, "Bearer")
        );
    }

    private void addRefreshTokenCookie(
            HttpServletResponse response,
            String refreshToken
    ) {
        ResponseCookie cookie = ResponseCookie
                .from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/auth")
                .maxAge(Duration.ofDays(refreshTokenExpirationDays))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(
                    name = "refresh_token",
                    required = false
            ) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            User user = refreshTokenService.rotate(refreshToken);

            String newAccessToken = jwtService.generateAcessToken(user);
            String newRefreshToken = refreshTokenService.create(user);

            addRefreshTokenCookie(response, newRefreshToken);

            return ResponseEntity.ok(
                    new LoginResponse(newAccessToken, "Bearer")
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(
                    name = "refresh_token",
                    required = false
            ) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.revoke(refreshToken);
        }

        clearRefreshTokenCookie(response);

        return ResponseEntity.noContent().build();
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie
                .from("refresh_token", "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/auth")
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

}
