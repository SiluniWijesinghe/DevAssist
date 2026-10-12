package com.devAssist.backend.service;

import com.devAssist.backend.dto.OpenRouterRequest;
import com.devAssist.backend.dto.OpenRouterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final LLMService llmService;
    private final ToolExecutor toolExecutor;


    public String chat(
            Long repositoryId,
            String userMessage
    ) {

        List<OpenRouterRequest.Message> messages =
                new ArrayList<>();

        messages.add(
                new OpenRouterRequest.Message(
                        "system",
                        """
                                You are DevAssist, an AI-powered software engineering assistant.
                                You are analyzing repository ID: %d.

                                You have tools to search repository code, read source files, and inspect Git history.

                                Investigation rules:
                                - For repository-specific questions, inspect the repository before answering.
                                - Search first when you do not know the relevant file path.
                                - Read relevant files; for cross-file questions, follow references into related files.
                                - Use Git history when asked about changes, commits, or historical context.
                                - readFile supports optional startLine and lineCount for large files.
                                - Avoid repeating an identical search without a reason.
                                - Treat tool output as the evidence for claims about this repository.
                                - Never invent classes, methods, files, line numbers, or commit details.
                                - Cite inspected code using repository-relative paths and line numbers when available.
                                - Separate confirmed observations from hypotheses and recommendations.
                                - If evidence is insufficient, state what you could not verify.
                                - Once enough evidence is collected, provide a clear, concise engineering answer.
                                """.formatted(repositoryId),
                        null,
                        null
                )
        );

        messages.add(
                new OpenRouterRequest.Message(
                        "user",
                        userMessage,
                        null,
                        null
                )
        );

        List<OpenRouterRequest.Tool> tools =
                createTools();

        int maxIterations = 5;

        for (int iteration = 0;
             iteration < maxIterations;
             iteration++) {

            OpenRouterResponse response =
                    llmService.chat(
                            messages,
                            tools
                    );

            OpenRouterResponse.Message assistantMessage =
                    response.choices()
                            .getFirst()
                            .message();

            if (assistantMessage.toolCalls() == null
                    || assistantMessage.toolCalls().isEmpty()) {

                return assistantMessage.content();
            }

            messages.add(
                    new OpenRouterRequest.Message(
                            "assistant",
                            assistantMessage.content(),
                            convertToolCalls(
                                    assistantMessage.toolCalls()
                            ),
                            null
                    )
            );

            for (OpenRouterResponse.ToolCall toolCall :
                    assistantMessage.toolCalls()) {

                String result =
                        toolExecutor.execute(
                                toolCall.function().name(),
                                toolCall.function().arguments()
                        );

                messages.add(
                        new OpenRouterRequest.Message(
                                "tool",
                                result,
                                null,
                                toolCall.id()
                        )
                );
            }
        }

        // We have executed the last permitted tool round. Give the model one final
        // synthesis pass with tools disabled so it cannot continue looping.
        OpenRouterResponse finalResponse = llmService.chat(messages, List.of());
        OpenRouterResponse.Message finalMessage = finalResponse.choices().getFirst().message();
        if (finalMessage.content() != null && !finalMessage.content().isBlank()) {
            return finalMessage.content();
        }
        return "I reached the investigation limit. Please narrow the question or ask about a specific file.";
    }

    private List<OpenRouterRequest.ToolCall> convertToolCalls(
            List<OpenRouterResponse.ToolCall> toolCalls
    ) {

        return toolCalls.stream()
                .map(call ->
                        new OpenRouterRequest.ToolCall(
                                call.id(),
                                call.type(),
                                new OpenRouterRequest.FunctionCall(
                                        call.function().name(),
                                        call.function().arguments()
                                )
                        )
                )
                .toList();
    }

    private List<OpenRouterRequest.Tool> createTools() {

        return List.of(

                new OpenRouterRequest.Tool(
                        "function",
                        new OpenRouterRequest.Function(
                                "searchRepository",
                                "Search the repository for files and code matching a query.",
                                new OpenRouterRequest.Parameters(
                                        "object",
                                        Map.of(
                                                "repositoryId",
                                                new OpenRouterRequest.Property(
                                                        "integer",
                                                        "The repository ID."
                                                ),
                                                "query",
                                                new OpenRouterRequest.Property(
                                                        "string",
                                                        "The text to search for."
                                                )
                                        ),
                                        List.of(
                                                "repositoryId",
                                                "query"
                                        )
                                )
                        )
                ),

                new OpenRouterRequest.Tool(
                        "function",
                        new OpenRouterRequest.Function(
                                "readFile",
                                "Read a repository file with line numbers. For large files, request a specific startLine and lineCount.",
                                new OpenRouterRequest.Parameters(
                                        "object",
                                        Map.of(
                                                "repositoryId",
                                                new OpenRouterRequest.Property(
                                                        "integer",
                                                        "The repository ID."
                                                ),
                                                "path",
                                                new OpenRouterRequest.Property(
                                                        "string",
                                                        "The repository-relative file path."
                                                ),
                                                "startLine",
                                                new OpenRouterRequest.Property(
                                                        "integer",
                                                        "Optional 1-based starting line. Defaults to 1."
                                                ),
                                                "lineCount",
                                                new OpenRouterRequest.Property(
                                                        "integer",
                                                        "Optional number of lines to read, up to 500. Defaults to 250."
                                                )
                                        ),
                                        List.of(
                                                "repositoryId",
                                                "path"
                                        )
                                )
                        )
                ),

                new OpenRouterRequest.Tool(
                        "function",
                        new OpenRouterRequest.Function(
                                "searchGitHistory",
                                "Search the Git history of the repository.",
                                new OpenRouterRequest.Parameters(
                                        "object",
                                        Map.of(
                                                "repositoryId",
                                                new OpenRouterRequest.Property(
                                                        "integer",
                                                        "The repository ID."
                                                ),
                                                "query",
                                                new OpenRouterRequest.Property(
                                                        "string",
                                                        "Text to search for in Git history."
                                                )
                                        ),
                                        List.of(
                                                "repositoryId",
                                                "query"
                                        )
                                )
                        )
                )
        );
    }
}