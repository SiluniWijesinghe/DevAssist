package com.devAssist.backend.controller;

import com.devAssist.backend.dto.RepositoryResponse;
import com.devAssist.backend.entity.Repository;
import com.devAssist.backend.service.RepositoryIngestionService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/repositories")
@RequiredArgsConstructor
@Validated
public class RepositoryController {

    private final RepositoryIngestionService repositoryIngestionService;

    @PostMapping
    public ResponseEntity<RepositoryResponse> ingestRepository(
            @RequestParam @NotBlank String url,
            @RequestParam @NotBlank String name
    ) {
        Repository repository = repositoryIngestionService.ingestRepository(url.trim(), name.trim());
        return ResponseEntity.ok(new RepositoryResponse(
                repository.getId(), repository.getName(), repository.getUrl(),
                repository.getCreatedAt(), repository.getFiles().size()
        ));
    }
}
