package com.devsclinic.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record TreatmentOptionRequest(
        @NotBlank(message = "Treatment name is required") String name,
        @NotBlank(message = "Category is required") String category,
        @Min(value = 0, message = "Price cannot be negative") double price,
        @Min(value = 0, message = "GST rate cannot be negative") double gstRate
) {
}
