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
}
