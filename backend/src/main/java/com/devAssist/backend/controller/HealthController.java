package com.devAssist.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

@RestController
public class HealthController {
    private final DataSource dataSource;

    public HealthController(DataSource dataSource) { this.dataSource = dataSource; }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> health() {
        try (Connection ignored = dataSource.getConnection()) {
            return ResponseEntity.ok(Map.of("status", "UP", "database", "UP"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("status", "DOWN", "database", "DOWN"));
        }
    }
}
