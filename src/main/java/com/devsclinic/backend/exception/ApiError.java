package com.devsclinic.backend.exception;

import java.time.Instant;
import java.util.List;

public record ApiError(int status, String message, List<String> errors, Instant timestamp) {

    public static ApiError of(int status, String message) {
        return new ApiError(status, message, List.of(), Instant.now());
    }

    public static ApiError of(int status, String message, List<String> errors) {
        return new ApiError(status, message, errors, Instant.now());
    }
}
