package com.devsclinic.backend.service;

import com.devsclinic.backend.dto.InventoryItemRequest;
import com.devsclinic.backend.exception.ResourceNotFoundException;
import com.devsclinic.backend.model.InventoryItem;
import com.devsclinic.backend.repository.InventoryItemRepository;
import com.devsclinic.backend.util.SequentialIdGenerator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final NotificationService notificationService;

    public InventoryService(InventoryItemRepository inventoryItemRepository, NotificationService notificationService) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.notificationService = notificationService;
    }

    public List<InventoryItem> getAll() {
        return inventoryItemRepository.findAll();
    }

    public InventoryItem getById(String id) {
        return inventoryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found: " + id));
    }

    public InventoryItem create(InventoryItemRequest request) {
        List<String> existingIds = inventoryItemRepository.findAll().stream().map(InventoryItem::getId).toList();
        String newId = SequentialIdGenerator.next(existingIds, "MED-", 101);

        boolean isLow = request.stock() <= request.threshold();

        InventoryItem item = InventoryItem.builder()
                .id(newId)
                .name(request.name())
                .category(request.category())
                .price(request.price())
                .stock(request.stock())
                .threshold(request.threshold())
                .expiry(request.expiry())
                .supplier(request.supplier())
                .barcode(request.barcode())
                .lowStockNotified(isLow)
                .build();

        InventoryItem saved = inventoryItemRepository.save(item);
        if (isLow) {
            notificationService.createLowStockNotification(saved);
        }
        return saved;
    }

    public InventoryItem update(String id, InventoryItemRequest request) {
        InventoryItem item = getById(id);
        boolean alreadyFlagged = item.isLowStockNotified();

        item.setName(request.name());
        item.setCategory(request.category());
        item.setPrice(request.price());
        item.setStock(request.stock());
        item.setThreshold(request.threshold());
        item.setExpiry(request.expiry());
        item.setSupplier(request.supplier());
        item.setBarcode(request.barcode());

        boolean isLowNow = item.getStock() <= item.getThreshold();
        item.setLowStockNotified(isLowNow);

        InventoryItem saved = inventoryItemRepository.save(item);
        if (isLowNow && !alreadyFlagged) {
            notificationService.createLowStockNotification(saved);
        } else if (!isLowNow && alreadyFlagged) {
            notificationService.resolveLowStockNotifications(saved.getId());
        }
        return saved;
    }

    public void delete(String id) {
        if (!inventoryItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Medicine not found: " + id);
        }
        inventoryItemRepository.deleteById(id);
        notificationService.resolveLowStockNotifications(id);
    }
}
