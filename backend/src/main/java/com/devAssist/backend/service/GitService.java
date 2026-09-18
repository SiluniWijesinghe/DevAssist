package com.devAssist.backend.service;

import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class GitService {

    public Path cloneRepository(String url, String repositoryName)
            throws Exception {

        Path repositoryPath =
                Files.createTempDirectory("devassist-" + repositoryName);

        Git.cloneRepository()
                .setURI(url)
                .setDirectory(repositoryPath.toFile())
                .call();

        return repositoryPath;
    }
}