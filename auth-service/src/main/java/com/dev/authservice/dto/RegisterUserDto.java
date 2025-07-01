package com.dev.authservice.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterUserDto(
        @NotBlank
        String firstname,

        @NotBlank
        String lastname,

        @NotBlank
        String email,

        @NotBlank
        String password
) {
}
