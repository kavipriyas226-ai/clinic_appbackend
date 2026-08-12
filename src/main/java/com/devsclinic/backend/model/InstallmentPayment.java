package com.devsclinic.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One installment payment recorded against an {@link Invoice} — e.g. "paid ₹10,000 on the 1st visit". */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstallmentPayment {
    private String id;
    private double amount;
    /** "UPI" | "Card" | "Cash" | "—". */
    private String method;
    /** ISO date string (yyyy-MM-dd). */
    private String date;
    /** Optional free-text note, e.g. "1st Visit". */
    private String note;
}
