package com.devsclinic.backend.service;

import com.devsclinic.backend.dto.PrescriptionItemRequest;
import com.devsclinic.backend.dto.PrescriptionRequest;
import com.devsclinic.backend.exception.ResourceNotFoundException;
import com.devsclinic.backend.model.Patient;
import com.devsclinic.backend.model.Prescription;
import com.devsclinic.backend.model.PrescriptionItem;
import com.devsclinic.backend.repository.PatientRepository;
import com.devsclinic.backend.repository.PrescriptionRepository;
import com.devsclinic.backend.util.SequentialIdGenerator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository, PatientRepository patientRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.patientRepository = patientRepository;
    }

    public List<Prescription> getByPatient(String patientId) {
        return prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    public Prescription create(PrescriptionRequest request) {
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + request.patientId()));

        List<String> existingIds = prescriptionRepository.findAll().stream().map(Prescription::getId).toList();
        String newId = SequentialIdGenerator.next(existingIds, "RX-", 1);

        List<PrescriptionItem> items = request.items().stream().map(this::toItem).toList();

        Prescription prescription = Prescription.builder()
                .id(newId)
                .patientId(patient.getId())
                .patientName(patient.getName())
                .items(items)
                .createdAt(Instant.now())
                .build();

        return prescriptionRepository.save(prescription);
    }

    private PrescriptionItem toItem(PrescriptionItemRequest r) {
        return PrescriptionItem.builder()
                .medicineId(r.medicineId())
                .name(r.name())
                .dosage(r.dosage())
                .frequency(r.frequency())
                .duration(r.duration())
                .build();
    }
}
