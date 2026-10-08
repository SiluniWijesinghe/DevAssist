package com.devAssist.backend.dto;

public record AgentChatRequest(
        Long repositoryId,
        String message
) {
}