package com.devsclinic.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record PrescriptionItemRequest(
        @NotBlank(message = "Medicine id is required") String medicineId,
        @NotBlank(message = "Medicine name is required") String name,
        String dosage,
        String frequency,
        String duration
) {
}
