package com.devAssist.backend.dto;


public record AuthResponse(
        String token,
        String tokenType,
        String email,
        long expiresInSeconds
    ) {}
