package com.devsclinic.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ClinicProfileRequest(
        @NotBlank(message = "Clinic name is required") String name,
        String tagline,
        String phone,
        String email,
        String gstin,
        String address,
        String logoDataUrl
) {
}
