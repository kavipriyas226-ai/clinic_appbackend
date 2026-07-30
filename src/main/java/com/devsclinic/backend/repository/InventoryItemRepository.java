package com.devsclinic.backend.repository;

import com.devsclinic.backend.model.InventoryItem;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InventoryItemRepository extends MongoRepository<InventoryItem, String> {
}
