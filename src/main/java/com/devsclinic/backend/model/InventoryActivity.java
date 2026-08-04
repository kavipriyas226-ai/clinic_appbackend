package com.devsclinic.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/** An audit-trail entry for a change to an inventory medicine, used to power the Inventory Dashboard's activity feed and stock-movement/low-stock-trend widgets. */
@Document(collection = "inventory_activities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryActivity {

    @Id
    private String id;

    /** "CREATED" | "STOCK_ADDED" | "STOCK_REMOVED" | "UPDATED" | "LOW_STOCK" | "DELETED" */
    private String type;

    private String message;

    /** The inventory item this activity refers to — itemId may no longer exist once DELETED. */
    private String itemId;
    private String itemName;

    /** Positive for stock added, negative for stock removed, null when not a stock-quantity change. */
    private Integer quantityChange;

    private Instant createdAt;
}
