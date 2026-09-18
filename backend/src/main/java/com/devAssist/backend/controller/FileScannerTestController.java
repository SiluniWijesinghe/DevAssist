package com.devAssist.backend.controller;

import com.devAssist.backend.dto.ScannedFile;
import com.devAssist.backend.service.FileScannerService;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.List;

@RestController
public class FileScannerTestController {

    private final FileScannerService fileScannerService;

    public FileScannerTestController(
            FileScannerService fileScannerService) {

        this.fileScannerService = fileScannerService;
    }
    
    public record ScanRequest(String path) {}

    @PostMapping("/api/files/test")
    public List<ScannedFile> scan(@RequestBody ScanRequest request) throws Exception {
        return fileScannerService.scanRepository(Path.of(request.path()));
    }
}