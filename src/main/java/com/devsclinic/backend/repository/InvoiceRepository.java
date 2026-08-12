package com.devsclinic.backend.repository;

import com.devsclinic.backend.model.Invoice;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InvoiceRepository extends MongoRepository<Invoice, String> {
    List<Invoice> findAllByPatientIdOrderByDateDesc(String patientId);
}
