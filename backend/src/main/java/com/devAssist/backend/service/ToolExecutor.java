package com.devAssist.backend.service;

import com.devAssist.backend.tool.GitHistoryResult;
import com.devAssist.backend.tool.ReadFileTool;
import com.devAssist.backend.tool.SearchGitHistoryTool;
import com.devAssist.backend.tool.SearchRepoTool;
import com.devAssist.backend.tool.SearchResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ToolExecutor {

    private final SearchRepoTool searchRepoTool;
    private final ReadFileTool readFileTool;
    private final SearchGitHistoryTool searchGitHistoryTool;
    private final JsonMapper objectMapper;

    public String execute(
            String toolName,
            String arguments
    ) {

        try {

            JsonNode args =
                    objectMapper.readTree(arguments);

            return switch (toolName) {

                case "searchRepository" ->
                        executeSearchRepository(args);

                case "readFile" ->
                        executeReadFile(args);

                case "searchGitHistory" ->
                        executeSearchGitHistory(args);

                default ->
                        throw new IllegalArgumentException(
                                "Unknown tool: " + toolName
                        );
            };

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(
                    "Failed to execute tool: " + toolName,
                    e
            );
        }
    }

    private String executeSearchRepository(JsonNode args) {

        Long repositoryId =
                args.get("repositoryId").asLong();

        String query =
                args.get("query").asText();

        List<SearchResult> results =
                searchRepoTool.searchRepository(
                        repositoryId,
                        query
                );

        return toJson(results);
    }

    private String executeReadFile(JsonNode args) {

        Long repositoryId =
                args.get("repositoryId").asLong();

        String path =
                args.get("path").asText();

        String content =
                readFileTool.readFile(
                        repositoryId,
                        path
                );

        return toJson(content);
    }

    private String executeSearchGitHistory(JsonNode args) {

        Long repositoryId =
                args.get("repositoryId").asLong();

        String query =
                args.get("query").asText();

        List<GitHistoryResult> results =
                searchGitHistoryTool.searchGitHistory(
                        repositoryId,
                        query
                );

        return toJson(results);
    }

    private String toJson(Object value) {

        try {
            return objectMapper.writeValueAsString(value);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to serialize tool result",
                    e
            );
        }
    }
}