package com.devAssist.backend.service;

import com.devAssist.backend.dto.AuthRequest;
import com.devAssist.backend.dto.AuthResponse;
import com.devAssist.backend.entity.User;
import com.devAssist.backend.repository.UserRepository;
import com.devAssist.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(AuthRequest request) {
        validatePassword(request.password());
        String email = normalize(request.email());
        if (users.existsByEmailIgnoreCase(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        users.save(new User(email, passwordEncoder.encode(request.password())));
        return tokenResponse(email);
    }

    public AuthResponse login(AuthRequest request) {
        validatePassword(request.password());
        String email = normalize(request.email());
        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        return tokenResponse(user.getEmail());
    }

    private AuthResponse tokenResponse(String email) {
        return new AuthResponse(jwtService.generate(email), "Bearer", email, jwtService.expirationSeconds());
    }

    private void validatePassword(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at most 72 UTF-8 bytes");
        }
    }
    private String normalize(String email) { return email.trim().toLowerCase(java.util.Locale.ROOT); }
}
