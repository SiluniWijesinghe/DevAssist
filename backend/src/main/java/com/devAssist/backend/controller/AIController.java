package com.devAssist.backend.controller;

import com.devAssist.backend.dto.AgentChatRequest;
import com.devAssist.backend.dto.AgentChatResponse;
import com.devAssist.backend.entity.ChatMessage;
import com.devAssist.backend.entity.Repository;
import com.devAssist.backend.repository.ChatMessageRepository;
import com.devAssist.backend.repository.GitRepoRepository;
import com.devAssist.backend.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AIController {

    private final AgentService agentService;
    private final GitRepoRepository gitRepoRepository;
    private final ChatMessageRepository chatMessageRepository;

    @PostMapping("/chat")
    public AgentChatResponse chat(@RequestBody AgentChatRequest request, Authentication authentication) {
        // agentService.chat already enforces ownership; load the repo for the FK afterwards
        String answer = agentService.chat(request.repositoryId(), request.message(), authentication.getName());

        var repo = gitRepoRepository.findByIdAndOwner_EmailIgnoreCase(request.repositoryId(), authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Repository not found"));
        save(repo, "user", request.message());
        save(repo, "assistant", answer);
        return new AgentChatResponse(answer);
    }

    private void save(Repository repo, String role, String content) {
        ChatMessage m = new ChatMessage();
        m.setRepository(repo);
        m.setRole(role);
        m.setContent(content);
        chatMessageRepository.save(m);
    }
}