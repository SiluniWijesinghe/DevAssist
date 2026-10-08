package com.devAssist.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record OpenRouterRequest(
        String model,
        List<Message> messages,
        List<Tool> tools,

        @JsonProperty("tool_choice")
        String toolChoice
) {

    public record Message(
            String role,
            String content,

            @JsonProperty("tool_calls")
            List<ToolCall> toolCalls,

            @JsonProperty("tool_call_id")
            String toolCallId
    ) {}

    public record Tool(
            String type,
            Function function
    ) {}

    public record Function(
            String name,
            String description,
            Parameters parameters
    ) {}

    public record Parameters(
            String type,
            Map<String, Property> properties,
            List<String> required
    ) {}

    public record Property(
            String type,
            String description
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

//{ the response JSON structure
//        "model": "openai/gpt-4o",
//        "messages": [
//        { "role": "user", "content": "What's the weather in Colombo?" }
//        ],
//        "tools": [
//        {
//        "type": "function",
//        "function": {
//        "name": "get_weather",
//        "description": "Get weather for a city",
//        "parameters": {
//        "type": "object",
//        "properties": {
//        "city": { "type": "string", "description": "City name" }
//        },
//        "required": ["city"]
//        }
//        }
//        }
//        ],
//        "tool_choice": "auto"
//        }