package com.devAssist.backend.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record AuthRequest(
        @Email @NotBlank @Size(max = 254) String email,
        @NotBlank @Size(min = 8, max = 72) String password
    ) {}
