package com.project.meeting_platform.auth.Service.Auth;

import com.project.meeting_platform.Model.User;
import com.project.meeting_platform.Repository.User.UserRepository;
import com.project.meeting_platform.auth.dto.User.LoginRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createAdmin(String name, String email, String rawPassword) {
        String normalizedName = validateName(name);
        String normalizedEmail = normalizeAndValidateEmail(email);

        validatePassword(rawPassword);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalStateException("Já existe um usuário com este e-mail.");
        }

        String passwordHash = passwordEncoder.encode(rawPassword);
        User user = new User(normalizedName, normalizedEmail, passwordHash);

        return userRepository.save(user);
    }

    public User authenticate(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException(
                        "E-mail ou senha inválidos."
                ));

        if (!user.isActive() ||
                !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("E-mail ou senha inválidos.");
        }

        return user;
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("O nome é obrigatório.");
        }

        return name.trim();
    }

    private String normalizeAndValidateEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("O e-mail é inválido.");
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void validatePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 12) {
            throw new IllegalArgumentException(
                    "A senha deve ter pelo menos 12 caracteres."
            );
        }
    }

    @Transactional(readOnly = true)
    public boolean hasRegisteredUsers() {
        return userRepository.count() > 0;
    }
}
