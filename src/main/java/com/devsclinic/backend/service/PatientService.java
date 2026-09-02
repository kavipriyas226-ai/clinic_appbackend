package com.devsclinic.backend.service;

import com.devsclinic.backend.dto.PatientCreateRequest;
import com.devsclinic.backend.dto.PatientUpdateRequest;
import com.devsclinic.backend.exception.ResourceNotFoundException;
import com.devsclinic.backend.model.Patient;
import com.devsclinic.backend.repository.PatientRepository;
import com.devsclinic.backend.util.SequentialIdGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<Patient> getAll() {
        return patientRepository.findAll();
    }

    public Patient getById(String id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + id));
    }

    public Patient create(PatientCreateRequest request) {
        List<String> existingIds = patientRepository.findAll().stream().map(Patient::getId).toList();
        String newId = SequentialIdGenerator.next(existingIds, "PT-", 1001);

        int age = computeAge(request.dob());

        Patient patient = Patient.builder()
                .id(newId)
                .name(request.name())
                .age(age)
                .gender(request.gender())
                .phone(request.phone())
                .concern(request.concern())
                .lastVisit(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .status("New")
                .doctor(request.doctor())
                .dob(request.dob())
                .email(request.email())
                .address(request.address())
                .bloodGroup(request.bloodGroup())
                .allergies(request.allergies())
                .medicalNotes(request.medicalNotes())
                .emergencyContact(request.emergencyContact())
                .referredBy(request.referredBy())
                .concernDescription(request.concernDescription())
                .existingMedications(request.existingMedications())
                .treatments(new ArrayList<>())
                .prescriptions(new ArrayList<>())
                .invoices(new ArrayList<>())
                .build();

        return patientRepository.save(patient);
    }

    public Patient update(String id, PatientUpdateRequest request) {
        Patient patient = getById(id);
        patient.setName(request.name());
        patient.setDob(request.dob());
        patient.setAge(computeAge(request.dob()));
        patient.setGender(request.gender());
        patient.setBloodGroup(request.bloodGroup());
        patient.setPhone(request.phone());
        patient.setEmail(request.email());
        patient.setAddress(request.address());
        patient.setEmergencyContact(request.emergencyContact());
        patient.setReferredBy(request.referredBy());
        patient.setConcern(request.concern());
        patient.setDoctor(request.doctor());
        patient.setConcernDescription(request.concernDescription());
        patient.setAllergies(request.allergies());
        patient.setExistingMedications(request.existingMedications());
        patient.setMedicalNotes(request.medicalNotes());
        return patientRepository.save(patient);
    }

    public void delete(String id) {
        if (!patientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Patient not found: " + id);
        }
        patientRepository.deleteById(id);
    }

    private int computeAge(String dob) {
        try {
            LocalDate birthDate = LocalDate.parse(dob);
            return Period.between(birthDate, LocalDate.now()).getYears();
        } catch (Exception e) {
            return 0;
        }
    }
}
