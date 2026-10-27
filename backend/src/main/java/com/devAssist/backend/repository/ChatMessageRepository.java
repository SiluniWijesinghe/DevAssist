package com.devAssist.backend.repository;

import com.devAssist.backend.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRepositoryIdOrderByIdAsc(Long repositoryId);
    @Transactional
    void deleteByRepositoryId(Long repositoryId);

    List<ChatMessage> findTop8ByRepositoryIdOrderByIdDesc(Long repositoryId);
}