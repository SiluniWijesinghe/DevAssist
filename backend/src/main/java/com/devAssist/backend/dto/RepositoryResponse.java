package com.devAssist.backend.dto;

import java.time.LocalDateTime;

public record RepositoryResponse(
        Long id,
        String name,
        String url,
        LocalDateTime createdAt,
        int indexedFiles,
        LocalDateTime indexedAt
) {
}
