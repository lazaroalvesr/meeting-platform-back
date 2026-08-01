package com.project.meeting_platform.auth.controller.Account;

import com.project.meeting_platform.Model.User;
import com.project.meeting_platform.auth.Service.Account.AccountService;
import com.project.meeting_platform.auth.Service.Auth.JwtService;
import com.project.meeting_platform.auth.Service.RefreshToken.RefreshTokenService;
import com.project.meeting_platform.auth.dto.User.AccountResponse;
import com.project.meeting_platform.auth.dto.User.ChangePasswordRequest;
import com.project.meeting_platform.auth.dto.User.UpdateAccountRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final long refreshTokenExpirationDays;
    private final boolean secureCookie;

    public AccountController(
            AccountService accountService,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            @Value("${jwt.refresh-token-expiration-days}") long refreshTokenExpirationDays,
            @Value("${app.cookie.secure}") boolean secureCookie
    ) {
        this.accountService = accountService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
        this.secureCookie = secureCookie;
    }

    @GetMapping
    public ResponseEntity<AccountResponse> get(Authentication authentication) {
        return ResponseEntity.ok(responseFor(accountService.getCurrentUser(authentication.getName()), null));
    }

    @PatchMapping
    public ResponseEntity<AccountResponse> update(
            @Valid @RequestBody UpdateAccountRequest request,
            Authentication authentication,
            HttpServletResponse response
    ) {
        User user = accountService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok(refreshSession(user, response));
    }

    @PostMapping("/change-password")
    public ResponseEntity<AccountResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication,
            HttpServletResponse response
    ) {
        User user = accountService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(refreshSession(user, response));
    }

    private AccountResponse refreshSession(User user, HttpServletResponse response) {
        refreshTokenService.revokeAll(user);
        String refreshToken = refreshTokenService.create(user);
        addRefreshTokenCookie(response, refreshToken);
        return responseFor(user, jwtService.generateAcessToken(user));
    }

    private AccountResponse responseFor(User user, String accessToken) {
        return new AccountResponse(user.getName(), user.getEmail(), accessToken, "Bearer");
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/auth")
                .maxAge(Duration.ofDays(refreshTokenExpirationDays))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
