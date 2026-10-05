package com.devAssist.backend.tool;

import com.devAssist.backend.entity.Repository;
import com.devAssist.backend.repository.GitRepoRepository;
import com.devAssist.backend.service.GitService;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SearchGitHistoryToolTest {

    private final GitRepoRepository gitRepoRepository =
            mock(GitRepoRepository.class);

    private final GitService gitService =
            mock(GitService.class);

    private final SearchGitHistoryTool searchGitHistoryTool =
            new SearchGitHistoryTool(
                    gitRepoRepository,
                    gitService
            );

    @Test
    void shouldSearchGitHistory() {

        // Arrange
        Repository repository = new Repository();

        repository.setId(1L);
        repository.setName("test-repository");
        repository.setUrl(
                "https://github.com/example/test-repository.git"
        );

        Path clonedRepository =
                Path.of("C:/temp/test-repository");

        GitHistoryResult commit1 =
                new GitHistoryResult(
                        "abc123",
                        "Fix authentication bug",
                        "John",
                        null
                );

        when(gitRepoRepository.findById(1L))
                .thenReturn(Optional.of(repository));

        when(gitService.cloneRepository(
                repository.getUrl(),
                repository.getName()
        )).thenReturn(clonedRepository);

        when(gitService.searchHistory(
                clonedRepository,
                "authentication"
        )).thenReturn(List.of(commit1));

        // Act
        List<GitHistoryResult> result =
                searchGitHistoryTool.searchGitHistory(
                        1L,
                        "authentication"
                );

        // Assert
        assertEquals(1, result.size());

        assertEquals(
                "abc123",
                result.get(0).commitId()
        );

        assertEquals(
                "Fix authentication bug",
                result.get(0).shortMessage()
        );

        verify(gitRepoRepository)
                .findById(1L);

        verify(gitService)
                .cloneRepository(
                        repository.getUrl(),
                        repository.getName()
                );

        verify(gitService)
                .searchHistory(
                        clonedRepository,
                        "authentication"
                );
    }

    @Test
    void shouldThrowExceptionWhenRepositoryDoesNotExist() {

        // Arrange
        when(gitRepoRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> searchGitHistoryTool.searchGitHistory(
                        999L,
                        "authentication"
                )
        );

        verify(gitRepoRepository)
                .findById(999L);

        // GitService must never be called
        verifyNoInteractions(gitService);
    }

    @Test
    void shouldReturnEmptyListWhenNoCommitsMatch() {

        // Arrange
        Repository repository = new Repository();

        repository.setId(1L);
        repository.setName("test-repository");
        repository.setUrl(
                "https://github.com/example/test-repository.git"
        );

        Path clonedRepository =
                Path.of("C:/temp/test-repository");

        when(gitRepoRepository.findById(1L))
                .thenReturn(Optional.of(repository));

        when(gitService.cloneRepository(
                repository.getUrl(),
                repository.getName()
        )).thenReturn(clonedRepository);

        when(gitService.searchHistory(
                clonedRepository,
                "nonexistent"
        )).thenReturn(List.of());

        // Act
        List<GitHistoryResult> result =
                searchGitHistoryTool.searchGitHistory(
                        1L,
                        "nonexistent"
                );

        // Assert
        assertTrue(result.isEmpty());
    }
}