package com.devsclinic.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One row of a patient's invoice history (embedded in {@link Patient}). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientInvoiceSummary {
    private String id;
    private String date;
    private double amount;
    private double amountPaid;
    private double balance;
    private String status;
}
