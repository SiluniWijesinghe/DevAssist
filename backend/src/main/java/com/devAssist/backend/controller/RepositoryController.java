package com.devAssist.backend.controller;

import com.devAssist.backend.entity.Repository;
import com.devAssist.backend.service.RepositoryIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/repositories")
@RequiredArgsConstructor
public class RepositoryController {

    private final RepositoryIngestionService repositoryIngestionService;


    @PostMapping
    public ResponseEntity<Repository> ingestRepository(
            @RequestParam String url,
            @RequestParam String name
    ) {

        Repository repository =
                repositoryIngestionService.ingestRepository(url, name);

        return ResponseEntity.ok(repository);
    }
}