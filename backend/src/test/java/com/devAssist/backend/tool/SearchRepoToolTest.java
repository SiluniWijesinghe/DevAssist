package com.devAssist.backend.tool;

import com.devAssist.backend.entity.Repository;
import com.devAssist.backend.entity.RepositoryFile;
import com.devAssist.backend.repository.RepoFileRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SearchRepoToolTest {

    private final RepoFileRepository repoFileRepository = mock(RepoFileRepository.class);

    private final SearchRepoTool searchRepoTool = new SearchRepoTool(repoFileRepository);

    @Test
    void shouldFindMatchingFiles() {

        Repository repository = new Repository();
        repository.setId(1L);

        RepositoryFile file = new RepositoryFile();

        file.setId(101L);

        file.setPath(
                "src/main/java/com/devAssist/service/UserService.java"
        );

        file.setFileName("UserService.java");

        file.setExtension(".java");

        file.setContent(
                "public class UserService { }"
        );

        file.setRepository(repository);

        when(repoFileRepository.findByRepositoryId(1L))
                .thenReturn(List.of(file));

        List<SearchResult> result =
                searchRepoTool.searchRepository(
                        1L,
                        "UserService"
                );

        assertEquals(1, result.size());

        assertEquals(
                "UserService.java",
                result.get(0).fileName()
        );

        verify(repoFileRepository)
                .findByRepositoryId(1L);
    }
    @Test
    void shouldReturnEmptyListWhenNoFilesMatch() {

        when(repoFileRepository
                .findByRepositoryIdAndPathContainingIgnoreCase(
                        1L,
                        "Payment"
                ))
                .thenReturn(List.of());

        List<SearchResult> result =
                searchRepoTool.searchRepository(
                        1L,
                        "Payment"
                );

        assertTrue(result.isEmpty());
    }
}