package com.repovision.repository.api.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterRepositoryRequest(

        @NotBlank(message = "Repository name must not be blank")
        String name,

        @NotBlank(message = "Repository path must not be blank")
        String localPath,

        @NotBlank(message = "Default branch must not be blank")
        String defaultBranch

) {
}