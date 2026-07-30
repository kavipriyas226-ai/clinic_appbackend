package com.devsclinic.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/** A system-generated notification (e.g. a low-stock alert raised from Inventory). */
@Document(collection = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    private String id;

    /** "LOW_STOCK" for now; open to more types later. */
    private String type;

    private String message;

    /** The inventory item this notification refers to. */
    private String itemId;
    private String itemName;
    private int stock;
    private int threshold;

    @Builder.Default
    private boolean read = false;

    private Instant createdAt;
}
