package com.devAssist.backend.tool;

import com.devAssist.backend.entity.RepositoryFile;
import com.devAssist.backend.repository.RepoFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SearchRepoTool {

    private final RepoFileRepository repoFileRepository;

    public List<SearchResult> searchRepository(Long repositoryId, String query) {

        String normalizedQuery = query.trim().toLowerCase();

        List<RepositoryFile> files =
                repoFileRepository.findByRepositoryId(repositoryId);

        return files.stream()
                .filter(file ->
                        matches(file, normalizedQuery)
                )
                .map(file ->
                        new SearchResult(
                                file.getPath(),
                                file.getFileName(),
                                file.getExtension(),
                                createSnippet(file, normalizedQuery)
                        )
                )
                .toList();
    }

    private boolean matches(RepositoryFile file, String query) {

        return file.getPath()
                .toLowerCase()
                .contains(query)

                || file.getFileName()
                .toLowerCase()
                .contains(query)

                || (file.getContent() != null
                && file.getContent()
                .toLowerCase()
                .contains(query));
    }

    private String createSnippet(RepositoryFile file, String query) {

        String content = file.getContent();

        if (content == null || content.isBlank()) {
            return "";
        }

        String lowerContent = content.toLowerCase();

        int index = lowerContent.indexOf(query);

        if (index == -1) {
            return content.length() > 300
                    ? content.substring(0, 300)
                    : content;
        }

        int start = Math.max(0, index - 150);
        int end = Math.min(
                content.length(),
                index + query.length() + 150
        );

        return content.substring(start, end);
    }
}