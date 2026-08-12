package com.devsclinic.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record InstallmentPaymentRequest(
        @Positive(message = "Payment amount must be greater than 0") double amount,
        @NotBlank(message = "Payment method is required") String method,
        @NotBlank(message = "Payment date is required") String date,
        String note
) {
}
