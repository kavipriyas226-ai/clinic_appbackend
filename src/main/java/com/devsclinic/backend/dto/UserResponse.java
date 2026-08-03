package com.devsclinic.backend.dto;

public record UserResponse(String id, String username, String role, boolean enabled) {
}
