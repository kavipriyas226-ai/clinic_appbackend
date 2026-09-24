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
    /** Editable line total — defaults to price*qty but can be manually overridden. */
    private double amount;

    /** GST rate applied to this line, as a percentage — copied from the selected Treatment's/
     * InventoryItem's own configured rate at the time this invoice was created. */
    private double gstRate;
    /** This line's amount after its proportional share of the invoice discount, before GST. */
    private double taxableAmount;
    private double cgstAmount;
    private double sgstAmount;
    private double igstAmount;
    /** cgstAmount + sgstAmount, or igstAmount for inter-state — this line's total GST. */
    private double gstAmount;
}
