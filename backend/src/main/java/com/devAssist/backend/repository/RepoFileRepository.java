package com.devAssist.backend.repository;

import com.devAssist.backend.entity.RepositoryFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepoFileRepository
        extends JpaRepository<RepositoryFile, Long> {

    List<RepositoryFile> findByRepositoryId(Long repositoryId);
}