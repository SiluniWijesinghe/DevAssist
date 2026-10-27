package com.devAssist.backend.security;

import com.devAssist.backend.repository.GitRepoRepository;
import com.devAssist.backend.service.ToolExecutor;
import com.devAssist.backend.tool.ReadFileTool;
import com.devAssist.backend.tool.SearchGitHistoryTool;
import com.devAssist.backend.tool.SearchRepoTool;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ToolExecutorOwnershipTest {
    private final SearchRepoTool search = mock(SearchRepoTool.class);
    private final ReadFileTool read = mock(ReadFileTool.class);
    private final SearchGitHistoryTool history = mock(SearchGitHistoryTool.class);
    private final GitRepoRepository repositories = mock(GitRepoRepository.class);
    private final ToolExecutor executor = new ToolExecutor(search, read, history, JsonMapper.builder().build(), repositories);

    @Test void toolCannotReadAnotherUsersRepository() {
        when(repositories.findByIdAndOwner_EmailIgnoreCase(77L, "attacker@example.com"))
                .thenReturn(Optional.empty());

        String result = executor.execute("readFile",
                "{\"repositoryId\":77,\"path\":\"secret.txt\"}", "attacker@example.com");

        assertTrue(result.contains("Repository not found"));
        verifyNoInteractions(read, search, history);
    }
}
