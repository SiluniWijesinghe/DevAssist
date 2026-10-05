package com.devAssist.backend.service;

import com.devAssist.backend.dto.ScannedFile;
import com.devAssist.backend.entity.Repository;
import com.devAssist.backend.entity.RepositoryFile;
import com.devAssist.backend.repository.RepoFileRepository;
import com.devAssist.backend.repository.GitRepoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepositoryIngestionService {

    private final GitService gitService;
    private final FileScannerService fileScannerService;
    private final GitRepoRepository gitRepoRepository;
    private final RepoFileRepository repoFileRepository;


    @Transactional
    public Repository ingestRepository(String url, String repositoryName)  {

        // 1. Clone repository
        Path repositoryPath =
                gitService.cloneRepository(url, repositoryName);

        // 2. Scan repository files
        List<ScannedFile> scannedFiles =
                fileScannerService.scanRepository(repositoryPath);

        // 3. Create and save Repository
        Repository repository = new Repository();

        repository.setName(repositoryName);
        repository.setUrl(url);
        repository.setCreatedAt(LocalDateTime.now());


        // 4. Convert scanned files into RepositoryFile entities
        for (ScannedFile scannedFile : scannedFiles) {

            RepositoryFile repositoryFile = new RepositoryFile();

            repositoryFile.setRepository(repository);
            repositoryFile.setPath(scannedFile.getPath());
            repositoryFile.setExtension(scannedFile.getExtension());
            repositoryFile.setContent(scannedFile.getContent());
            repositoryFile.setFileName(scannedFile.getFileName());
            repositoryFile.setSizeBytes(scannedFile.getSizeBytes());

            repository.addFile(repositoryFile);
        }
        return gitRepoRepository.save(repository);
    }
}