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
    private double total;

    /** "Paid" or "Unpaid". */
    private String status;
    /** "UPI" | "Card" | "Cash" | "—". */
    private String method;
}
