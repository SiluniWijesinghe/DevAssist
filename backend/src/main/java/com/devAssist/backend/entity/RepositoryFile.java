package com.devAssist.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "repository_files")
@Data
public class RepositoryFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private String fileName;

    private String extension;

    @Column(nullable = false)
    private Long sizeBytes;

    @Lob
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private Repository repository;

    // getters and setters
}