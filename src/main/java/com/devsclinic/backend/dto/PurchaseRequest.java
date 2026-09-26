package com.devsclinic.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PurchaseRequest(
        @NotBlank(message = "Supplier name is required") String supplierName,
        String address,
        String gstin,
        @NotEmpty(message = "At least one line item is required") @Valid List<PurchaseLineItemRequest> lineItems
) {
}
