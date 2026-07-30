package com.devsclinic.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One row of a patient's treatment history (embedded in {@link Patient}). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientTreatment {
    private String date;
    private String name;
    private String doctor;
    private String notes;
}
