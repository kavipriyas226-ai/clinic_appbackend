package com.devsclinic.backend.dto;

/**
 * Summary figures for the Auditor Dashboard. Billing-derived figures (everything except
 * {@code totalPatients} and {@code inventoryValue}) are filtered to the requested date range;
 * {@code totalPatients} is an all-time count and {@code inventoryValue} is a current-stock
 * snapshot, since neither is meaningfully "date ranged" in the existing data.
 */
public record AuditorDashboardResponse(
        double totalBillingAmount,
        double totalCollected,
        double totalPending,
        double totalDiscounts,
        double totalCgst,
        double totalSgst,
        double inventoryValue,
        int totalInvoiceCount,
        int totalPatients,
        int totalBillingTransactions
) {
}
