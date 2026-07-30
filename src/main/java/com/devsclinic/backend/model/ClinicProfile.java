package com.devsclinic.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Singleton document holding the clinic's own profile shown on invoices/reports. */
@Document(collection = "clinic_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicProfile {

    @Id
    private String id;

    private String name;
    private String tagline;
    private String phone;
    private String email;
    private String gstin;
    private String address;
    /** Optional uploaded logo, stored as a data URI (base64). Null = use the default static logo. */
    private String logoDataUrl;
}
