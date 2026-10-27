package com.devAssist.backend.service;

import com.devAssist.backend.dto.ScannedFile;
import com.devAssist.backend.entity.Repository;
import com.devAssist.backend.entity.RepositoryFile;
import com.devAssist.backend.entity.User;
import com.devAssist.backend.repository.GitRepoRepository;
import com.devAssist.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepositoryIngestionService {

    private final GitService gitService;
    private final FileScannerService fileScannerService;
    private final GitRepoRepository gitRepoRepository;
    private final UserRepository userRepository;

    @Transactional
    public Repository ingestRepository(String url, String repositoryName, String ownerEmail) {
        User owner = userRepository.findByEmailIgnoreCase(ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User account not found"));
        if (url == null || !url.matches("https://github\\.com/[^/\\s]+/[^/\\s]+/?")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only public GitHub repository URLs are supported");
        }

        Path repositoryPath = gitService.cloneRepository(url, repositoryName);
        List<ScannedFile> scannedFiles = fileScannerService.scanRepository(repositoryPath);

        LocalDateTime now = LocalDateTime.now();
        Repository repository = new Repository();
        repository.setName(repositoryName);
        repository.setOwner(owner);
        repository.setUrl(url);
        repository.setCreatedAt(now);
        repository.setIndexedAt(now);
        attachFiles(repository, scannedFiles);
        return gitRepoRepository.save(repository);
    }

    /** Re-clones the repo and replaces its indexed files. The old index is kept if the clone fails. */
    @Transactional
    public Repository resync(Long id, String ownerEmail) {
        Repository repository = gitRepoRepository.findByIdAndOwner_EmailIgnoreCase(id, ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Repository not found"));

        Path repositoryPath = gitService.cloneRepository(repository.getUrl(), repository.getName());
        List<ScannedFile> scannedFiles = fileScannerService.scanRepository(repositoryPath);

        repository.getFiles().clear();            // orphanRemoval deletes the old rows
        attachFiles(repository, scannedFiles);
        repository.setIndexedAt(LocalDateTime.now());
        return gitRepoRepository.save(repository);
    }

    private void attachFiles(Repository repository, List<ScannedFile> scannedFiles) {
        for (ScannedFile f : scannedFiles) {
            RepositoryFile file = new RepositoryFile();
            file.setRepository(repository);
            file.setPath(f.getPath());
            file.setExtension(f.getExtension());
            file.setContent(f.getContent());
            file.setFileName(f.getFileName());
            file.setSizeBytes(f.getSizeBytes());
            repository.addFile(file);
        }
    }
}