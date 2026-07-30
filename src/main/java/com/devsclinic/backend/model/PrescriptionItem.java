package com.devsclinic.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One prescribed medicine within a {@link Prescription}. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionItem {
    private String medicineId;
    private String name;
    private String dosage;
    private String frequency;
    private String duration;
}
