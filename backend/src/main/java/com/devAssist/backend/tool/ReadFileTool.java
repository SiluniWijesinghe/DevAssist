package com.devAssist.backend.tool;

import com.devAssist.backend.entity.RepositoryFile;
import com.devAssist.backend.repository.RepoFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadFileTool {

    private final RepoFileRepository repoFileRepository;

    public String readFile(Long repositoryId, String path) {

        RepositoryFile file = repoFileRepository.findByRepositoryIdAndPath(repositoryId, path)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "File not found: " + path
                                )
                        );

        return file.getContent();
    }
}