package com.devsclinic.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/** A billing invoice, created from the Billing page or via Pharmacy's "Proceed to Billing". */
@Document(collection = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    private String id;

    private String patientId;
    private String patientName;
    /** ISO date string (yyyy-MM-dd). */
    private String date;

    @Builder.Default
    private List<LineItem> lineItems = new ArrayList<>();

    private boolean discountEnabled;
    private double discountPercent;
    private boolean gstEnabled;

    private double subtotal;
    private double discountAmount;
    private double gstAmount;
    /** The total treatment amount owed for this invoice. */
    private double total;

    /** Installment/EMI-style payments recorded against this invoice's total, one per visit. */
    @Builder.Default
    private List<InstallmentPayment> payments = new ArrayList<>();

    /** Sum of {@link #payments} — recomputed whenever a payment is added, edited, or removed. */
    private double amountPaid;
    /** {@link #total} minus {@link #amountPaid}, floored at 0. */
    private double balance;

    /** "Pending" | "Partially Paid" | "Fully Paid" — derived from amountPaid vs total. */
    private String status;
    /** Method of the most recent payment ("UPI" | "Card" | "Cash"), or "—" if nothing paid yet. */
    private String method;
}
