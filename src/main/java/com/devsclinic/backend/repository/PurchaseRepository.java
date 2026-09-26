package com.devsclinic.backend.repository;

import com.devsclinic.backend.model.Purchase;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PurchaseRepository extends MongoRepository<Purchase, String> {
}
