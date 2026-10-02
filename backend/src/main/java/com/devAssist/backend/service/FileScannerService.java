package com.devAssist.backend.service;

import com.devAssist.backend.dto.ScannedFile;
import com.devAssist.backend.exception.FileScanException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FileScannerService {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            "java",
            "js",
            "jsx",
            "ts",
            "tsx",
            "py",
            "cs",
            "html",
            "css",
            "sql",
            "xml",
            "json",
            "yml",
            "yaml",
            "md"
    );

    private static final long MAX_FILE_SIZE = 1_000_000;

    public List<ScannedFile> scanRepository(Path repositoryPath)  {

        List<ScannedFile> scannedFiles = new ArrayList<>();

        try (var paths = Files.walk(repositoryPath)) {

            paths
                    .filter(Files::isRegularFile)
                    .filter(path -> !isInsideGitDirectory(
                            repositoryPath, path))
                    .filter(this::isSupportedFile)
                    .filter(this::isWithinSizeLimit)
                    .forEach(path -> {

                        scannedFiles.add(scanFile(
                                repositoryPath,
                                path
                        ));
                    });
        }catch(IOException e){
            throw  new FileScanException(e.getMessage());
        }

        return scannedFiles;
    }

    private ScannedFile scanFile(Path repositoryPath, Path filePath)  {
        try {
            String content = Files.readString(
                    filePath,
                    StandardCharsets.UTF_8
            );

            String relativePath =
                    repositoryPath
                            .relativize(filePath)
                            .toString()
                            .replace("\\", "/");

            String fileName =
                    filePath.getFileName().toString();

            String extension =
                    getExtension(fileName);

            long size =
                    Files.size(filePath);

            return new ScannedFile(
                    relativePath,
                    fileName,
                    extension,
                    size,
                    content
            );
        }catch (IOException | FileScanException e){
            throw new FileScanException(e.getMessage());
        }
    }

    private boolean isInsideGitDirectory(Path repositoryPath, Path filePath) {

        Path relativePath =
                repositoryPath.relativize(filePath);

        return relativePath
                .startsWith(".git");
    }

    private boolean isSupportedFile(Path path) {

        String fileName =
                path.getFileName().toString();

        String extension =
                getExtension(fileName);

        return SUPPORTED_EXTENSIONS.contains(extension);
    }

    private boolean isWithinSizeLimit(Path path) {

        try {
            return Files.size(path) <= MAX_FILE_SIZE;
        } catch (IOException e) {
            return false;
        }
    }

    private String getExtension(String fileName) {

        int lastDot = fileName.lastIndexOf('.');

        if (lastDot == -1 ||
                lastDot == fileName.length() - 1) {

            return "";
        }

        return fileName
                .substring(lastDot + 1)
                .toLowerCase();
    }
}