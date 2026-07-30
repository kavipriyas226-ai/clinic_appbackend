package com.devsclinic.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record InvoiceRequest(
        @NotBlank(message = "Patient is required") String patientId,
        @NotEmpty(message = "At least one line item is required") @Valid List<LineItemRequest> lineItems,
        boolean discountEnabled,
        double discountPercent,
        boolean gstEnabled
) {
}
