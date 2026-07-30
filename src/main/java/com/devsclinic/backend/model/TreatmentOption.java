package com.devsclinic.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** A billable clinic service/procedure (Acne Treatment, Hair Fall Treatment, Botox Consultation, etc). */
@Document(collection = "treatment_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TreatmentOption {

    @Id
    private String id;

    private String name;
    /** "Skin Treatments" | "Hair Treatments" | "Cosmetic Procedures". */
    private String category;
    private double price;
}
