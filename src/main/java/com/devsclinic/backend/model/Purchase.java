package com.devsclinic.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/** A recorded purchase from a supplier — increases inventory stock and is a GST-relevant
 * record in its own right, separate from Billing (which is money coming in from patients). */
@Document(collection = "purchases")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Purchase {

    @Id
    private String id;

    // Supplier (party) details.
    private String supplierName;
    private String address;
    /** Supplier's GSTIN, optional — not every supplier is GST-registered. */
    private String gstin;

    /** ISO date string (yyyy-MM-dd). */
    private String date;

    @Builder.Default
    private List<PurchaseLineItem> lineItems = new ArrayList<>();

    private double subtotal;
    private double gstAmount;
    /** {@link #subtotal} plus {@link #gstAmount}. */
    private double total;
}
