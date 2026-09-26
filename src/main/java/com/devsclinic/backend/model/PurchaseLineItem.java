package com.devsclinic.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One line item of a {@link Purchase} — always a Product (InventoryItem); Purchase has no
 * concept of buying a Treatment/service. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseLineItem {
    /** id of the referenced InventoryItem. */
    private String refId;
    private String name;
    private String hsnSacCode;
    private double gstPercent;
    private double rate;
    private int qty;
    /** Editable line total — defaults to rate*qty but can be manually overridden. Treated as
     * the taxable (pre-GST) amount for this line. */
    private double taxableAmount;
    private double gstAmount;
    /** {@link #taxableAmount} plus {@link #gstAmount}. */
    private double totalAmount;
}
