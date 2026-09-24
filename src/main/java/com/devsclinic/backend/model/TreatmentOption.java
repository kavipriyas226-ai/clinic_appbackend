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
    /** GST rate as a percentage (0/5/12/18/...). Boxed so "never configured" (null, backfilled
     * once by DataSeeder) is distinguishable from an intentional 0% (tax-exempt) rate. */
    private Double gstRate;
}
