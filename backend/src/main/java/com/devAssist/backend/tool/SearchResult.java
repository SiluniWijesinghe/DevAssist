package com.devAssist.backend.tool;

public record SearchResult(
        String path,
        String fileName,
        String extension,
        String snippet
) {
}