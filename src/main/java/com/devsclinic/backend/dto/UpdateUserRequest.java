package com.devsclinic.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @NotBlank(message = "Username is required") String username,
        @NotBlank(message = "Role is required") String role,
        boolean enabled,
        /** Optional — null/blank leaves the current password unchanged. Validated for length in the service layer. */
        String newPassword
) {
}
