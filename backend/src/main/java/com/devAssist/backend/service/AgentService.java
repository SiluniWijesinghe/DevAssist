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
                                                                           
                                 You have tools available to inspect the repository.
                                                                           
                                IMPORTANT:
                                - When the user asks about the repository, you MUST use the
                                  appropriate tool instead of saying that you will use it.
                                - Do not describe a tool call in natural language.
                                - Actually call the tool.
                                - Use the tool results to answer the user.
                                - You may call multiple tools if necessary.
                                - Do not invent repository information.
                                - After gathering enough information, stop using tools and provide
                                  the final answer.
                                - Do not call a tool if the available information is already
                                  sufficient to answer the user's question.
                                                                
                                After gathering enough information, provide a clear engineering answer.
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

            System.out.println("===== ASSISTANT RESPONSE =====");
            System.out.println("Content: " + assistantMessage.content());
            System.out.println("Tool Calls: " + assistantMessage.toolCalls());
            System.out.println("==============================");

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

        throw new IllegalStateException(
                "Agent exceeded maximum tool iterations"
        );
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
                                "Read the complete contents of a repository file.",
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