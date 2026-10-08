package com.devAssist.backend.controller;

import com.devAssist.backend.dto.AgentChatRequest;
import com.devAssist.backend.dto.AgentChatResponse;
import com.devAssist.backend.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AIController {

    private final AgentService agentService;

    @PostMapping("/chat")
    public AgentChatResponse chat(
            @RequestBody AgentChatRequest request
    ) {

        String answer = agentService.chat(
                request.repositoryId(),
                request.message()
        );

        return new AgentChatResponse(answer);
    }
}