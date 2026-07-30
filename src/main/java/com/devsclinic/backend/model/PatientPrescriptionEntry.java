package com.devsclinic.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One row of a patient's prescription history (embedded in {@link Patient}). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientPrescriptionEntry {
    private String date;
    private String medicine;
    private String dosage;
}
