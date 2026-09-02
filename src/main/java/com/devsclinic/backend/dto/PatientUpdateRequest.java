package com.devsclinic.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** Mirrors every field on the Register Patient form (see PatientCreateRequest) so
 * Edit Patient can update the same record in place — no field is silently dropped. */
public record PatientUpdateRequest(
        @NotBlank(message = "Full name is required") String name,
        @NotBlank(message = "Date of birth is required") String dob,
        @NotBlank(message = "Gender is required") String gender,
        String bloodGroup,
        @NotBlank(message = "Phone number is required") String phone,
        String email,
        String address,
        String emergencyContact,
        String referredBy,
        @NotBlank(message = "Concern type is required") String concern,
        @NotBlank(message = "Assigned doctor is required") String doctor,
        String concernDescription,
        String allergies,
        String existingMedications,
        String medicalNotes
) {
}
