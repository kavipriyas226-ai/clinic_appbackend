package com.devsclinic.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record InventoryItemRequest(
        @NotBlank(message = "Medicine name is required") String name,
        @NotBlank(message = "Category is required") String category,
        @Min(value = 0, message = "Price cannot be negative") double price,
        @Min(value = 0, message = "Stock cannot be negative") int stock,
        @Min(value = 0, message = "Threshold cannot be negative") int threshold,
        @NotBlank(message = "Expiry date is required") String expiry,
        @NotBlank(message = "Supplier is required") String supplier
) {
}
