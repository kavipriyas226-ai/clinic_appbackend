package com.devsclinic.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ChangeAccountRequest(
        @NotBlank(message = "Email is required") @Email(message = "Must be a valid email") String username,
        /** Only required when newPassword is also provided. */
        String currentPassword,
        /** Optional — leave blank to keep the current password. */
        String newPassword
) {
}
