package com.devsclinic.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/** An audit-trail entry for a change to a financial or inventory record, written by
 * {@link com.devsclinic.backend.service.AuditLogService} at the point of mutation in
 * BillingService/InventoryService. Read-only from the API's perspective — nothing ever
 * updates or deletes an entry once written. */
@Document(collection = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    private String id;

    /** "Billing" | "Payment" | "Inventory" */
    private String module;

    /** e.g. "Invoice Created", "Invoice Cancelled", "Payment Added", "Stock Adjusted" */
    private String action;

    /** The invoice ID / inventory item ID this entry refers to. */
    private String recordId;

    /** One-line human-readable description of what happened. */
    private String summary;

    /** Null when the action has no meaningful "before" state (e.g. a creation). */
    private String oldValue;

    /** Null when the action has no meaningful "after" state (e.g. a deletion). */
    private String newValue;

    private String userName;
    private String userRole;

    private Instant timestamp;
}
