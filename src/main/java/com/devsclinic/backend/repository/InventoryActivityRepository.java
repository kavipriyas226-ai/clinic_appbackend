package com.devsclinic.backend.repository;

import com.devsclinic.backend.model.InventoryActivity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InventoryActivityRepository extends MongoRepository<InventoryActivity, String> {
    List<InventoryActivity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
