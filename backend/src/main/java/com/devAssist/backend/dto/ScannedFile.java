package com.devAssist.backend.dto;

public class ScannedFile {

    private String path;
    private String fileName;
    private String extension;
    private long sizeBytes;
    private String content;

    public ScannedFile(
            String path,
            String fileName,
            String extension,
            long sizeBytes,
            String content) {

        this.path = path;
        this.fileName = fileName;
        this.extension = extension;
        this.sizeBytes = sizeBytes;
        this.content = content;
    }

    public String getPath() {
        return path;
    }

    public String getFileName() {
        return fileName;
    }

    public String getExtension() {
        return extension;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getContent() {
        return content;
    }
}