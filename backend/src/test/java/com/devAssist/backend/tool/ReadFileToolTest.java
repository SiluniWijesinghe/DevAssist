package com.devAssist.backend.tool;

import com.devAssist.backend.entity.RepositoryFile;
import com.devAssist.backend.repository.RepoFileRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReadFileToolTest {

    private final RepoFileRepository repoFileRepository =
            mock(RepoFileRepository.class);

    private final ReadFileTool readFileTool =
            new ReadFileTool(repoFileRepository);

    @Test
    void shouldReadFileContent() {

        RepositoryFile file = new RepositoryFile();

        file.setPath(
                "src/main/java/com/devAssist/UserService.java"
        );

        file.setContent("""
                public class UserService {

                    public void createUser() {
                        // implementation
                    }
                }
                """);

        when(repoFileRepository
                .findByRepositoryIdAndPath(
                        1L,
                        "src/main/java/com/devAssist/UserService.java"
                ))
                .thenReturn(Optional.of(file));

        String result =
                readFileTool.readFile(
                        1L,
                        "src/main/java/com/devAssist/UserService.java"
                );

        assertTrue(result.contains("createUser"));
        assertTrue(result.contains("UserService"));
    }

    @Test
    void shouldThrowExceptionWhenFileDoesNotExist() {

        when(repoFileRepository
                .findByRepositoryIdAndPath(
                        1L,
                        "does/not/exist.java"
                ))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> readFileTool.readFile(
                        1L,
                        "does/not/exist.java"
                )
        );
    }
}