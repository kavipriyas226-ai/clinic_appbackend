package com.devsclinic.backend.dto;

/** Matches exactly what PatientDetails' edit mode lets the user change. */
public record PatientUpdateRequest(
        String phone,
        String email,
        String address,
        String allergies,
        String medicalNotes
) {
}
