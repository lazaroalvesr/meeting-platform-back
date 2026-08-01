package com.project.meeting_platform.auth.Service.Account;

import com.project.meeting_platform.Model.User;
import com.project.meeting_platform.Repository.User.UserRepository;
import com.project.meeting_platform.auth.dto.User.ChangePasswordRequest;
import com.project.meeting_platform.auth.dto.User.UpdateAccountRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public User getCurrentUser(String authenticatedEmail) {
        return findCurrentUser(authenticatedEmail);
    }

    @Transactional
    public User updateProfile(String authenticatedEmail, UpdateAccountRequest request) {
        User user = findCurrentUser(authenticatedEmail);
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        boolean emailChanged = !user.getEmail().equalsIgnoreCase(normalizedEmail);

        if (emailChanged && !passwordEncoder.matches(
                request.currentPassword() == null ? "" : request.currentPassword(),
                user.getPasswordHash()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe sua senha atual para alterar o e-mail."
            );
        }

        if (userRepository.existsByEmailAndIdNot(normalizedEmail, user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está em uso.");
        }

        user.updateProfile(request.name().trim(), normalizedEmail);
        return user;
    }

    @Transactional
    public User changePassword(String authenticatedEmail, ChangePasswordRequest request) {
        User user = findCurrentUser(authenticatedEmail);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha atual está incorreta.");
        }

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        return user;
    }

    private User findCurrentUser(String authenticatedEmail) {
        return userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado."));
    }
}
