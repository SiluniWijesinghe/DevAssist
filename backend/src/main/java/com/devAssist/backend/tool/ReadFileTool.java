package com.devAssist.backend.tool;

import com.devAssist.backend.entity.RepositoryFile;
import com.devAssist.backend.repository.RepoFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReadFileTool {

    private static final int DEFAULT_START_LINE = 1;
    private static final int DEFAULT_LINE_COUNT = 250;
    private static final int MAX_LINE_COUNT = 500;
    private static final int MAX_CHARACTERS = 24_000;

    private final RepoFileRepository repoFileRepository;

    public String readFile(Long repositoryId, String path) {
        return readFile(repositoryId, path, DEFAULT_START_LINE, DEFAULT_LINE_COUNT);
    }

    public String readFile(Long repositoryId, String path, int startLine, int requestedLineCount) {
        if (repositoryId == null) throw new IllegalArgumentException("repositoryId is required");
        if (path == null || path.isBlank()) throw new IllegalArgumentException("path is required");
        if (startLine < 1) throw new IllegalArgumentException("startLine must be at least 1");
        if (requestedLineCount < 1) throw new IllegalArgumentException("lineCount must be at least 1");

        RepositoryFile file = repoFileRepository.findByRepositoryIdAndPath(repositoryId, path)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + path));

        String content = file.getContent();
        if (content == null) return "File has no stored content: " + path;

        String[] lines = content.split("\\R", -1);
        if (startLine > lines.length) {
            return "Requested startLine " + startLine + " exceeds file length of " + lines.length
                    + " lines for " + path;
        }

        int lineCount = Math.min(requestedLineCount, MAX_LINE_COUNT);
        int endExclusive = Math.min(lines.length, startLine - 1 + lineCount);
        List<String> numberedLines = new ArrayList<>();
        int characters = 0;
        int actualEndLine = startLine - 1;

        for (int i = startLine - 1; i < endExclusive; i++) {
            String numbered = String.format("%5d | %s", i + 1, lines[i]);
            if (characters + numbered.length() > MAX_CHARACTERS) break;
            numberedLines.add(numbered);
            characters += numbered.length() + 1;
            actualEndLine = i + 1;
        }

        StringBuilder result = new StringBuilder();
        result.append("File: ").append(file.getPath())
                .append("\nLines ").append(startLine).append("-").append(actualEndLine)
                .append(" of ").append(lines.length).append("\n");
        result.append(String.join("\n", numberedLines));
        if (actualEndLine < lines.length) {
            result.append("\n[Output truncated. Request readFile again with startLine=")
                    .append(actualEndLine + 1).append(" to continue.]");
        }
        return result.toString();
    }
}
