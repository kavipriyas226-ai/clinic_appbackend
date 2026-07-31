package com.devsclinic.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record InvoiceUpdateRequest(
        @NotBlank(message = "Status is required") String status,
        @NotBlank(message = "Method is required") String method
) {
}
