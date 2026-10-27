package com.devAssist.backend.service;

import com.devAssist.backend.exception.GitOperationException;
import com.devAssist.backend.tool.GitHistoryResult;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class GitService {

    @Value("${app.repos-dir:${java.io.tmpdir}/devassist-repos}")
    private String reposDir;

    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    /** Returns a local, up-to-date clone of the repo. Cloned once, then updated with fetch. */
    public Path cloneRepository(String url, String repositoryName) {
        String key = UUID.nameUUIDFromBytes(
                url.replaceAll("/+$", "").toLowerCase().getBytes(StandardCharsets.UTF_8)).toString();
        Path dir = Path.of(reposDir, key);

        synchronized (locks.computeIfAbsent(key, k -> new Object())) {
            try {
                if (Files.isDirectory(dir.resolve(".git"))) {
                    try (Git git = Git.open(dir.toFile())) {
                        git.fetch().setRemoveDeletedRefs(true).call();
                        String branch = git.getRepository().getBranch();
                        git.reset().setMode(ResetCommand.ResetType.HARD).setRef("origin/" + branch).call();
                        return dir;
                    } catch (Exception e) {
                        // Corrupt clone or default branch renamed: start over below
                        FileSystemUtils.deleteRecursively(dir);
                    }
                }
                Files.createDirectories(dir.getParent());
                Git.cloneRepository().setURI(url).setDirectory(dir.toFile()).call().close();
                return dir;
            } catch (IOException | GitAPIException e) {
            deleteQuietly(dir);   // don't leave a half-cloned folder behind
            throw new GitOperationException("Clone failed for " + url, e);
        }
        }
    }
    private void deleteQuietly(Path dir) {
        try {
            FileSystemUtils.deleteRecursively(dir);
        } catch (IOException ignored) {
            // best effort: if cleanup fails, the next call will retry or re-clone
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