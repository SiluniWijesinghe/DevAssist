package com.devAssist.backend.service;

import com.devAssist.backend.exception.GitOperationException;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class GitService {

    public Path cloneRepository(String url, String repositoryName)  {
        try {
            Path repositoryPath =
                    Files.createTempDirectory("devassist-" + repositoryName);

            Git.cloneRepository()
                    .setURI(url)
                    .setDirectory(repositoryPath.toFile())
                    .call();

            return repositoryPath;
        }catch(IOException | GitAPIException e){
            throw new GitOperationException("Clone failed for " + url, e);
        }
    }
}