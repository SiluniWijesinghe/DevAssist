package com.devAssist.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleStatus(org.springframework.web.server.ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of("error", ex.getReason() != null ? ex.getReason() : "Request failed"));
    }

    @ExceptionHandler({org.springframework.web.bind.MethodArgumentNotValidException.class,
            jakarta.validation.ConstraintViolationException.class})
    public ResponseEntity<Map<String, String>> handleValidation(Exception ex) {
        return ResponseEntity.badRequest().body(Map.of("error", "Please check the values you entered."));
    }
    @ExceptionHandler(GitOperationException.class)
    public ResponseEntity<Map<String, String>> handleGit(GitOperationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(FileScanException.class)
    public ResponseEntity<Map<String, String>> handleFileScan(FileScanException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(DevAssistException.class)
    public ResponseEntity<Map<String, String>> handleDevAssist(DevAssistException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Something went wrong"));
    }
}