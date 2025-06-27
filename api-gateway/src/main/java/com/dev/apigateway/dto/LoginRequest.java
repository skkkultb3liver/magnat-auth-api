package com.dev.apigateway.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank
        String username, // email or username

        @NotBlank
        String password
) {
}
