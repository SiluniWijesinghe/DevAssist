package com.devAssist.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenRouterResponse(
        List<Choice> choices
) {

    public record Choice(
            Message message
    ) {}

    public record Message(
            String role,
            String content,

            @JsonProperty("tool_calls")
            List<ToolCall> toolCalls
    ) {}

    public record ToolCall(
            String id,
            String type,
            FunctionCall function
    ) {}

    public record FunctionCall(
            String name,
            String arguments
    ) {}
}