package com.devsclinic.backend.repository;

import com.devsclinic.backend.model.Invoice;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InvoiceRepository extends MongoRepository<Invoice, String> {
}
