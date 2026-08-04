package com.devsclinic.backend.service;

import com.devsclinic.backend.model.InventoryActivity;
import com.devsclinic.backend.model.InventoryItem;
import com.devsclinic.backend.repository.InventoryActivityRepository;
import com.devsclinic.backend.util.SequentialIdGenerator;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class InventoryActivityService {

    private final InventoryActivityRepository inventoryActivityRepository;

    public InventoryActivityService(InventoryActivityRepository inventoryActivityRepository) {
        this.inventoryActivityRepository = inventoryActivityRepository;
    }

    public List<InventoryActivity> getRecent(int limit) {
        return inventoryActivityRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }

    public void logCreated(InventoryItem item) {
        save("CREATED", item.getName() + " was added to inventory (" + item.getStock() + " units)", item, item.getStock());
    }

    public void logStockChange(InventoryItem item, int delta) {
        if (delta == 0) return;
        String type = delta > 0 ? "STOCK_ADDED" : "STOCK_REMOVED";
        String verb = delta > 0 ? "Added " : "Removed ";
        String noun = Math.abs(delta) == 1 ? " unit " : " units ";
        String prep = delta > 0 ? "to " : "from ";
        save(type, verb + Math.abs(delta) + noun + prep + item.getName(), item, delta);
    }

    public void logUpdated(InventoryItem item) {
        save("UPDATED", "Updated details for " + item.getName(), item, null);
    }

    public void logLowStock(InventoryItem item) {
        save("LOW_STOCK", item.getName() + " dropped to low stock (" + item.getStock() + " left)", item, null);
    }

    public void logDeleted(InventoryItem item) {
        save("DELETED", item.getName() + " was removed from inventory", item, item.getStock() > 0 ? -item.getStock() : null);
    }

    private void save(String type, String message, InventoryItem item, Integer quantityChange) {
        List<String> existingIds = inventoryActivityRepository.findAll().stream().map(InventoryActivity::getId).toList();
        String newId = SequentialIdGenerator.next(existingIds, "ACT-", 1);

        InventoryActivity activity = InventoryActivity.builder()
                .id(newId)
                .type(type)
                .message(message)
                .itemId(item.getId())
                .itemName(item.getName())
                .quantityChange(quantityChange)
                .createdAt(Instant.now())
                .build();

        inventoryActivityRepository.save(activity);
    }
}
