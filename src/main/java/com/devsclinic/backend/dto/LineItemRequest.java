package com.devsclinic.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record LineItemRequest(
        @NotBlank(message = "Line item reference id is required") String refId,
        @NotBlank(message = "Line item type is required") String type,
        @NotBlank(message = "Line item name is required") String name,
        @Min(value = 0, message = "Price cannot be negative") double price,
        @Min(value = 1, message = "Quantity must be at least 1") int qty,
        @Min(value = 0, message = "Amount cannot be negative") double amount,
        @Min(value = 0, message = "GST rate cannot be negative") double gstRate
) {
}
