package com.devAssist.backend.repository;

import com.devAssist.backend.entity.RepositoryFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepoFileRepository extends JpaRepository<RepositoryFile, Long> {
    List<RepositoryFile> findByRepositoryId(Long repositoryId);
    List<RepositoryFile> findByRepositoryIdAndPathContainingIgnoreCase(Long Id, String path);
    Optional<RepositoryFile> findByRepositoryIdAndPath(Long repositoryId, String path);
}