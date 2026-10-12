package com.devAssist.backend.tool;

import com.devAssist.backend.entity.RepositoryFile;
import com.devAssist.backend.repository.RepoFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class SearchRepoTool {

    private static final int CANDIDATE_LIMIT = 100;
    private static final int RESULT_LIMIT = 12;
    private static final int SNIPPET_RADIUS = 180;

    private final RepoFileRepository repoFileRepository;

    public List<SearchResult> searchRepository(Long repositoryId, String query) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("repositoryId is required");
        }
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query must not be blank");
        }

        String normalizedQuery = query.trim();
        List<RepositoryFile> candidates = repoFileRepository
                .searchCandidates(repositoryId, normalizedQuery,
                        PageRequest.of(0, CANDIDATE_LIMIT));

        return candidates.stream()
                .limit(RESULT_LIMIT)
                .map(file -> new SearchResult(
                        file.getPath(),
                        file.getFileName(),
                        file.getExtension(),
                        createSnippet(file, normalizedQuery)))
                .toList();
    }

    private String createSnippet(RepositoryFile file, String query) {
        String content = file.getContent();
        if (content == null || content.isBlank()) return "";

        String lowerContent = content.toLowerCase(Locale.ROOT);
        int index = lowerContent.indexOf(query.toLowerCase(Locale.ROOT));
        if (index < 0) return content.substring(0, Math.min(content.length(), SNIPPET_RADIUS * 2));

        int start = Math.max(0, index - SNIPPET_RADIUS);
        int end = Math.min(content.length(), index + query.length() + SNIPPET_RADIUS);
        return (start > 0 ? "…" : "") + content.substring(start, end)
                + (end < content.length() ? "…" : "");
    }
}
