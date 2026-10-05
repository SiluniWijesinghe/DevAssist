package com.devAssist.backend.tool;

import com.devAssist.backend.entity.Repository;
import com.devAssist.backend.repository.GitRepoRepository;
import com.devAssist.backend.service.GitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SearchGitHistoryTool {

    private final GitRepoRepository gitRepoRepository;
    private final GitService gitService;

    public List<GitHistoryResult> searchGitHistory(Long repositoryId, String query) {

        Repository repository =
                gitRepoRepository.findById(repositoryId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Repository not found: "
                                                + repositoryId
                                )
                        );

        Path clonedRepository =
                gitService.cloneRepository(
                        repository.getUrl(),
                        repository.getName()
                );

        return gitService.searchHistory(
                clonedRepository,
                query
        );
    }
}