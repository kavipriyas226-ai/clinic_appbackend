package com.devsclinic.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Username is required") String username,
        @NotBlank(message = "Password is required") String password,
        /** "ADMIN" or "USER" — must match the account's actual role. */
        @NotBlank(message = "Login type is required") String loginType
) {
}
