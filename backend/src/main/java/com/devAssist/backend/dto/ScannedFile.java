package com.devAssist.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScannedFile {

    private String path;
    private String fileName;
    private String extension;
    private long sizeBytes;
    private String content;


}