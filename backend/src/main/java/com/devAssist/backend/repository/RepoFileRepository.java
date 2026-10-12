package com.devAssist.backend.repository;

import com.devAssist.backend.entity.RepositoryFile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RepoFileRepository extends JpaRepository<RepositoryFile, Long> {

    @Query("""
            select f from RepositoryFile f
            where f.repository.id = :repositoryId
              and (
                lower(f.path) like lower(concat('%', :query, '%'))
                or lower(f.fileName) like lower(concat('%', :query, '%'))
                or lower(coalesce(f.content, '')) like lower(concat('%', :query, '%'))
              )
            order by
              case
                when lower(f.fileName) = lower(:query) then 0
                when lower(f.path) = lower(:query) then 1
                when lower(f.fileName) like lower(concat('%', :query, '%')) then 2
                when lower(f.path) like lower(concat('%', :query, '%')) then 3
                else 4
              end,
              f.path asc
            """)
    java.util.List<RepositoryFile> searchCandidates(
            @Param("repositoryId") Long repositoryId,
            @Param("query") String query,
            Pageable pageable
    );

    Optional<RepositoryFile> findByRepositoryIdAndPath(Long repositoryId, String path);
}
