package com.devAssist.backend.controller;

import com.devAssist.backend.service.GitService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
public class GitTestController {

    private final GitService gitService;

    public GitTestController(GitService gitService) {
        this.gitService = gitService;
    }

    @GetMapping("/api/git/test")
    public String testClone(@RequestParam String url) throws Exception {

        String repositoryName = extractRepositoryName(url);

        Path clonedPath =
                gitService.cloneRepository(url, repositoryName);

        return "Repository cloned successfully to: " + clonedPath;
    }

    private String extractRepositoryName(String url) {

        String cleanedUrl = url.endsWith("/")
                ? url.substring(0, url.length() - 1)
                : url;

        String repositoryName =
                cleanedUrl.substring(cleanedUrl.lastIndexOf("/") + 1);

        if (repositoryName.endsWith(".git")) {
            repositoryName =
                    repositoryName.substring(0, repositoryName.length() - 4);
        }

        return repositoryName;
    }
}