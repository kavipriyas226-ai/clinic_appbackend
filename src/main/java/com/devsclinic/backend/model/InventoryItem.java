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
    /** GST rate as a percentage (0/5/12/18/...). Boxed so "never configured" (null, backfilled
     * once by DataSeeder) is distinguishable from an intentional 0% (tax-exempt) rate. */
    private Double gstRate;

    /** Internal bookkeeping: true once a low-stock notification has been raised for the current dip. */
    @JsonIgnore
    @Builder.Default
    private boolean lowStockNotified = false;
}
