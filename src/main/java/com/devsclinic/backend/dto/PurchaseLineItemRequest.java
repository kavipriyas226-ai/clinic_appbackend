package com.devsclinic.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PurchaseLineItemRequest(
        @NotBlank(message = "Line item reference id is required") String refId,
        @NotBlank(message = "Line item name is required") String name,
        @Min(value = 0, message = "Rate cannot be negative") double rate,
        @Min(value = 1, message = "Quantity must be at least 1") int qty,
        @Min(value = 0, message = "Taxable amount cannot be negative") double taxableAmount
) {
}
