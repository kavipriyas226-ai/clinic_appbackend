package com.devsclinic.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * A patient record. Merges what used to be two separate mock structures
 * (`patients` list + `patientDetailExtra` map) into one real document per patient.
 */
@Document(collection = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    private String id;

    // Core / list-view fields
    private String name;
    private int age;
    private String gender;
    private String phone;
    private String concern;
    private String lastVisit;
    private String status; // New | Inactive
    private String doctor;

    // Detail fields
    private String dob;
    private String email;
    private String address;
    private String bloodGroup;
    private String allergies;
    private String medicalNotes;

    // Captured at registration, not shown on every view yet but persisted
    private String emergencyContact;
    private String referredBy;
    private String concernDescription;
    private String existingMedications;

    @Builder.Default
    private List<PatientTreatment> treatments = new ArrayList<>();
    @Builder.Default
    private List<PatientPrescriptionEntry> prescriptions = new ArrayList<>();
    @Builder.Default
    private List<PatientInvoiceSummary> invoices = new ArrayList<>();
}
