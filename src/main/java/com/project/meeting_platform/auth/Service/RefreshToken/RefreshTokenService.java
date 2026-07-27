package com.project.meeting_platform.auth.Service.RefreshToken;

import com.project.meeting_platform.Model.RefreshToken;
import com.project.meeting_platform.Model.User;
import com.project.meeting_platform.Repository.User.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class RefreshTokenService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_SIZE_BYTES = 64;

    private final RefreshTokenRepository refreshTokenRepository;
    private final long expirationDays;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${jwt.refresh-token-expiration-days}") long expirationDays
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.expirationDays = expirationDays;
    }

    @Transactional
    public String create(User user) {
        String rawToken = generateRawToken();
        String tokenHash = hash(rawToken);

        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHash,
                Instant.now().plusSeconds(expirationDays * 86_400)
        );

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional
    public User rotate(String rawToken) {
        RefreshToken currentToken = findValidToken(rawToken);

        currentToken.revoke();

        return currentToken.getUser();
    }

    @Transactional(readOnly = true)
    public User validateAndGetUser(String rawToken) {
        return findValidToken(rawToken).getUser();
    }

    private RefreshToken findValidToken(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Refresh token inválido."
                ));

        if (refreshToken.isRevoked() ||
                refreshToken.isExpired() ||
                !refreshToken.getUser().isActive()) {
            throw new IllegalArgumentException(
                    "Refresh token inválido."
            );
        }

        return refreshToken;
    }

    private String generateRawToken() {
        byte[] bytes = new byte[TOKEN_SIZE_BYTES];
        SECURE_RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));

            return java.util.HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 não está disponível.",
                    exception
            );
        }
    }

    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository
                .findByTokenHash(hash(rawToken))
                .ifPresent(refreshToken -> {
                    if (!refreshToken.isRevoked()) {
                        refreshToken.revoke();
                    }
                });
    }
}
