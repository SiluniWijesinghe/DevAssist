package com.devAssist.backend.repository;

import com.devAssist.backend.entity.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GitRepoRepository extends JpaRepository<Repository, Long> {
    Optional<Repository> findByUrl(String url);
    Optional<Repository> findByIdAndOwner_EmailIgnoreCase(Long id, String email);
    List<Repository> findAllByOwner_EmailIgnoreCaseOrderByCreatedAtDesc(String email);
}
