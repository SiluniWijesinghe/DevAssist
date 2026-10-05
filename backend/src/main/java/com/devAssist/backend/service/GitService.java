package com.devAssist.backend.service;

import com.devAssist.backend.exception.GitOperationException;
import com.devAssist.backend.tool.GitHistoryResult;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.stereotype.Service;
import com.devAssist.backend.tool.GitHistoryResult;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

    public List<GitHistoryResult> searchHistory(Path repositoryPath, String query) {

        try (Git git = Git.open(repositoryPath.toFile())) {

            Repository repository = git.getRepository();

            try (RevWalk revWalk = new RevWalk(repository)) {

                revWalk.markStart(
                        revWalk.parseCommit(
                                repository.resolve("HEAD")
                        )
                );

                List<GitHistoryResult> results =
                        new ArrayList<>();

                for (RevCommit commit : revWalk) {

                    String message =
                            commit.getFullMessage();

                    if (message
                            .toLowerCase()
                            .contains(query.toLowerCase())) {

                        results.add(
                                new GitHistoryResult(
                                        commit.getName(),
                                        commit.getShortMessage(),
                                        commit.getAuthorIdent()
                                                .getName(),
                                        commit.getAuthorIdent()
                                                .getWhen()
                                                .toInstant()
                                )
                        );
                    }
                }

                return results;
            }

        } catch (IOException e) {

            throw new GitOperationException(
                    "Failed to search Git history",
                    e
            );
        }
    }
}