package com.devAssist.backend.controller;

import com.devAssist.backend.dto.RepositoryResponse;
import com.devAssist.backend.entity.Repository;
import com.devAssist.backend.repository.ChatMessageRepository;
import com.devAssist.backend.repository.GitRepoRepository;
import com.devAssist.backend.repository.RepoFileRepository;
import com.devAssist.backend.service.RepositoryIngestionService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/repositories")
@RequiredArgsConstructor
@Validated
public class RepositoryController {
    private final RepositoryIngestionService repositoryIngestionService;
    private final GitRepoRepository gitRepoRepository;
    private final RepoFileRepository repoFileRepository;
    private final ChatMessageRepository chatMessageRepository;

    @PostMapping
    public ResponseEntity<RepositoryResponse> ingestRepository(@RequestParam @NotBlank @Size(max = 500) String url,
                                                               @RequestParam @NotBlank @Size(max = 100) String name,
                                                               Authentication authentication) {
        Repository repository = repositoryIngestionService.ingestRepository(url.trim(), name.trim(), authentication.getName());
        return ResponseEntity.ok(response(repository));
    }

    @PostMapping("/{id}/sync")
    public RepositoryResponse sync(@PathVariable Long id, Authentication authentication) {
        return response(repositoryIngestionService.resync(id, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRepository(@PathVariable Long id, Authentication authentication) {
        Repository repository = gitRepoRepository.findByIdAndOwner_EmailIgnoreCase(id, authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Repository not found"));
        gitRepoRepository.delete(repository);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<RepositoryResponse> listRepositories(Authentication authentication) {
        return gitRepoRepository.findAllByOwner_EmailIgnoreCaseOrderByCreatedAtDesc(authentication.getName())
                .stream().map(this::response).toList();
    }

    private RepositoryResponse response(Repository repository) {
        return new RepositoryResponse(repository.getId(), repository.getName(), repository.getUrl(),
                repository.getCreatedAt(), repoFileRepository.countByRepositoryId(repository.getId()),
                repository.getIndexedAt() != null ? repository.getIndexedAt() : repository.getCreatedAt()
        );
    }

    @GetMapping("/{id}/messages")
    public List<Map<String, String>> messages(@PathVariable Long id, Authentication authentication) {
        gitRepoRepository.findByIdAndOwner_EmailIgnoreCase(id, authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Repository not found"));
        return chatMessageRepository.findByRepositoryIdOrderByIdAsc(id).stream()
                .map(m -> Map.of("role", m.getRole(), "content", m.getContent(),"createdAt", m.getCreatedAt().toString())).toList();
    }

    @DeleteMapping("/{id}/messages")
    public ResponseEntity<Void> clearMessages(@PathVariable Long id, Authentication authentication) {
        gitRepoRepository.findByIdAndOwner_EmailIgnoreCase(id, authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Repository not found"));
        chatMessageRepository.deleteByRepositoryId(id);
        return ResponseEntity.noContent().build();
    }
}
