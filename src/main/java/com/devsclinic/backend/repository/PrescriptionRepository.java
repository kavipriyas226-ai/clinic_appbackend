package com.devsclinic.backend.repository;

import com.devsclinic.backend.model.Prescription;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PrescriptionRepository extends MongoRepository<Prescription, String> {
    List<Prescription> findByPatientId(String patientId);

    List<Prescription> findByPatientIdOrderByCreatedAtDesc(String patientId);
}
