package com.devsclinic.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One line item of an {@link Invoice} — a Treatment or a Medicine. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineItem {
    /** id of the referenced TreatmentOption or InventoryItem. */
    private String refId;
    /** "Treatment" or "Medicine". */
    private String type;
    private String name;
    private double price;
    private int qty;
    /** Editable line total — defaults to price*qty but can be manually overridden. Treated as
     * the taxable (pre-GST) amount for this line. */
    private double amount;

    /** HSN (goods) or SAC (services) code, resolved server-side from the referenced product at
     * the time this invoice was created — a historical snapshot, not a live lookup. */
    private String hsnSacCode;
    /** The GST rate actually applied to this line, resolved the same way. */
    private double gstPercent;
    /** GST computed for this line — 0 whenever the invoice's overall GST toggle is off. */
    private double gstAmount;
    /** {@link #amount} plus {@link #gstAmount}. */
    private double totalAmount;
}
