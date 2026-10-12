package com.devAssist.backend.service;

import com.devAssist.backend.tool.GitHistoryResult;
import com.devAssist.backend.tool.ReadFileTool;
import com.devAssist.backend.tool.SearchGitHistoryTool;
import com.devAssist.backend.tool.SearchRepoTool;
import com.devAssist.backend.tool.SearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ToolExecutor {

    private final SearchRepoTool searchRepoTool;
    private final ReadFileTool readFileTool;
    private final SearchGitHistoryTool searchGitHistoryTool;
    private final JsonMapper objectMapper;

    public String execute(String toolName, String arguments) {
        try {
            JsonNode args = objectMapper.readTree(arguments);
            return switch (toolName) {
                case "searchRepository" -> executeSearchRepository(args);
                case "readFile" -> executeReadFile(args);
                case "searchGitHistory" -> executeSearchGitHistory(args);
                default -> toJson(Map.of("error", "Unknown tool: " + toolName));
            };
        } catch (Exception e) {
            // Return tool failures to the model so it can recover or explain the limitation.
            return toJson(Map.of(
                    "error", e.getMessage() == null ? "Tool execution failed" : e.getMessage(),
                    "tool", toolName
            ));
        }
    }

    private String executeSearchRepository(JsonNode args) {
        long repositoryId = requiredLong(args, "repositoryId");
        String query = requiredText(args, "query");
        List<SearchResult> results = searchRepoTool.searchRepository(repositoryId, query);
        return toJson(results);
    }

    private String executeReadFile(JsonNode args) {
        long repositoryId = requiredLong(args, "repositoryId");
        String path = requiredText(args, "path");
        int startLine = args.has("startLine") && !args.get("startLine").isNull()
                ? args.get("startLine").asInt() : 1;
        int lineCount = args.has("lineCount") && !args.get("lineCount").isNull()
                ? args.get("lineCount").asInt() : 250;
        return toJson(readFileTool.readFile(repositoryId, path, startLine, lineCount));
    }

    private String executeSearchGitHistory(JsonNode args) {
        long repositoryId = requiredLong(args, "repositoryId");
        String query = requiredText(args, "query");
        List<GitHistoryResult> results = searchGitHistoryTool.searchGitHistory(repositoryId, query);
        return toJson(results);
    }

    private long requiredLong(JsonNode args, String field) {
        if (args == null || !args.has(field) || args.get(field).isNull()) {
            throw new IllegalArgumentException("Missing required argument: " + field);
        }
        return args.get(field).asLong();
    }

    private String requiredText(JsonNode args, String field) {
        if (args == null || !args.has(field) || args.get(field).isNull()
                || args.get(field).asText().isBlank()) {
            throw new IllegalArgumentException("Missing or blank required argument: " + field);
        }
        return args.get(field).asText();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{\"error\":\"Failed to serialize tool result\"}";
        }
    }
}
