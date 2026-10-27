package com.devAssist.backend.controller;

import com.devAssist.backend.dto.AgentChatRequest;
import com.devAssist.backend.dto.AgentChatResponse;
import com.devAssist.backend.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AIController {

    private final AgentService agentService;

    @PostMapping("/chat")
    public AgentChatResponse chat(
            @RequestBody AgentChatRequest request, Authentication authentication
    ) {

        String answer = agentService.chat(
                request.repositoryId(),
                request.message(),
                authentication.getName()
        );

        return new AgentChatResponse(answer);
    }
}