package com.devAssist.backend.controller;

import com.devAssist.backend.AI.LLMService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LLMTestController {

    private final LLMService llmService;

    @GetMapping("/api/ai/test")
    public String test(@RequestParam String prompt) {
        return llmService.ask(prompt);
    }
}