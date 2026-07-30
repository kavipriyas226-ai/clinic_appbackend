package com.devsclinic.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PatientGrowthResponse(
        String month,
        @JsonProperty("new") int newPatients,
        @JsonProperty("returning") int returningPatients
) {
}
