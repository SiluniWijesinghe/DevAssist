package com.devAssist.backend.controller;

import com.devAssist.backend.dto.AuthRequest;
import com.devAssist.backend.dto.AuthResponse;
import com.devAssist.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register") public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.status(201).body(authService.register(request));
    }

    @PostMapping("/login") public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request);
    }


    @PostMapping("/logout") public Map<String, String> logout() {
        return Map.of("message", "Logged out. Discard the bearer token on the client.");
    }

    @GetMapping("/me") public Map<String, String> me(Authentication authentication) {
        return Map.of("email", authentication.getName());
    }
}
