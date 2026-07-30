package com.devsclinic.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PrescriptionRequest(
        @NotBlank(message = "Patient is required") String patientId,
        @NotEmpty(message = "At least one medicine is required") @Valid List<PrescriptionItemRequest> items
) {
}
