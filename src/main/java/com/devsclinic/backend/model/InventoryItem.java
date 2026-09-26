package com.devsclinic.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** A medicine/product tracked in the pharmacy inventory. */
@Document(collection = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItem {

    @Id
    private String id;

    private String name;
    private String category;
    private double price;
    private int stock;
    private int threshold;
    /** ISO date string (yyyy-MM-dd), matching the frontend's date input format. */
    private String expiry;
    private String supplier;
    /** Scanned product barcode (EAN/UPC/etc.), optional — used to look up this item during barcode-scan stock updates. */
    private String barcode;

    /** GST rate for this product, e.g. 18 for 18%. Null means not yet set — callers fall back
     * to the historical flat 18% so pre-existing products keep billing exactly as before. */
    private Double gstPercent;
    /** HSN (goods) or SAC (services) code for GST filing, optional. */
    private String hsnSacCode;
    /** Whether {@link #price} is the pre-tax "TAXABLE" amount or a GST-"INCLUSIVE" total.
     * Defaults to TAXABLE, matching how price has always been treated. */
    @Builder.Default
    private String priceType = "TAXABLE";

    /** Internal bookkeeping: true once a low-stock notification has been raised for the current dip. */
    @JsonIgnore
    @Builder.Default
    private boolean lowStockNotified = false;
}
