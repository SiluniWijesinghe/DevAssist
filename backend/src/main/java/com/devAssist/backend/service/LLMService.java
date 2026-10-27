package com.devAssist.backend.service;

import com.devAssist.backend.config.OpenRouterConfig;
import com.devAssist.backend.dto.OpenRouterRequest;
import com.devAssist.backend.dto.OpenRouterResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class LLMService {

    private final RestClient restClient;
    private final OpenRouterConfig config;

    public LLMService(OpenRouterConfig config) {

        this.config = config;

        this.restClient = RestClient.builder()
                .baseUrl(config.baseUrl())
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + config.apiKey()
                )
                .defaultHeader(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }

    public OpenRouterResponse chat(
            List<OpenRouterRequest.Message> messages,
            List<OpenRouterRequest.Tool> tools
    ) {

        OpenRouterRequest request =
                new OpenRouterRequest(
                        config.model(),
                        messages,
                        tools,
                        "auto"
                );

        OpenRouterResponse response =
                restClient.post()
                        .uri("/chat/completions")
                        .body(request)
                        .retrieve()
                        .body(OpenRouterResponse.class);

        if (response == null
                || response.choices() == null
                || response.choices().isEmpty()
                || response.choices().getFirst().message() == null) {

            throw new IllegalStateException(
                    "OpenRouter returned an empty response"
            );
        }

        return response;
    }
}