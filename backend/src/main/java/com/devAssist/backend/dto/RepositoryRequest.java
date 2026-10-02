package com.devAssist.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RepositoryRequest {

    @NotBlank
    @Pattern(
            regexp = "https://github\\.com/[^/]+/[^/]+/?",
            message = "Must be a valid GitHub repository URL"
    )
    private String url;

}