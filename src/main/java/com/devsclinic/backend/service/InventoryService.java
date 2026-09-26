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
    private final InventoryActivityService inventoryActivityService;
    private final AuditLogService auditLogService;

    public InventoryService(
            InventoryItemRepository inventoryItemRepository,
            NotificationService notificationService,
            InventoryActivityService inventoryActivityService,
            AuditLogService auditLogService
    ) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.notificationService = notificationService;
        this.inventoryActivityService = inventoryActivityService;
        this.auditLogService = auditLogService;
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
                .gstPercent(request.gstPercent())
                .hsnSacCode(request.hsnSacCode())
                .priceType(request.priceType() != null && !request.priceType().isBlank() ? request.priceType() : "TAXABLE")
                .lowStockNotified(isLow)
                .build();

        InventoryItem saved = inventoryItemRepository.save(item);
        inventoryActivityService.logCreated(saved);
        if (isLow) {
            notificationService.createLowStockNotification(saved);
            inventoryActivityService.logLowStock(saved);
        }

        auditLogService.record("Inventory", "Inventory Item Created", saved.getId(),
                saved.getName() + " added to inventory (" + saved.getStock() + " units @ ₹" + saved.getPrice() + ")",
                null,
                "Stock: " + saved.getStock() + ", Price: ₹" + saved.getPrice() + ", Supplier: " + saved.getSupplier());

        return saved;
    }

    public InventoryItem update(String id, InventoryItemRequest request) {
        InventoryItem item = getById(id);
        boolean alreadyFlagged = item.isLowStockNotified();
        int previousStock = item.getStock();
        double previousPrice = item.getPrice();

        item.setName(request.name());
        item.setCategory(request.category());
        item.setPrice(request.price());
        item.setStock(request.stock());
        item.setThreshold(request.threshold());
        item.setExpiry(request.expiry());
        item.setSupplier(request.supplier());
        item.setBarcode(request.barcode());
        item.setGstPercent(request.gstPercent());
        item.setHsnSacCode(request.hsnSacCode());
        item.setPriceType(request.priceType() != null && !request.priceType().isBlank() ? request.priceType() : "TAXABLE");

        boolean isLowNow = item.getStock() <= item.getThreshold();
        item.setLowStockNotified(isLowNow);

        InventoryItem saved = inventoryItemRepository.save(item);

        int stockDelta = saved.getStock() - previousStock;
        if (stockDelta != 0) {
            inventoryActivityService.logStockChange(saved, stockDelta);
        } else {
            inventoryActivityService.logUpdated(saved);
        }

        auditLogService.record("Inventory", stockDelta != 0 ? "Stock Adjusted" : "Inventory Item Updated", saved.getId(),
                saved.getName() + (stockDelta != 0 ? (stockDelta > 0 ? " — added " : " — removed ") + Math.abs(stockDelta) + " units" : " — details updated"),
                "Stock: " + previousStock + ", Price: ₹" + previousPrice,
                "Stock: " + saved.getStock() + ", Price: ₹" + saved.getPrice());

        if (isLowNow && !alreadyFlagged) {
            notificationService.createLowStockNotification(saved);
            inventoryActivityService.logLowStock(saved);
        } else if (!isLowNow && alreadyFlagged) {
            notificationService.resolveLowStockNotifications(saved.getId());
        }
        return saved;
    }

    /** Increases (or decreases) a product's stock from a source other than the Inventory
     * edit form — e.g. a recorded Purchase — reusing the same stock-change logging and
     * low-stock notification handling as {@link #update}. */
    public InventoryItem addStock(String id, int qtyDelta) {
        InventoryItem item = getById(id);
        boolean alreadyFlagged = item.isLowStockNotified();

        item.setStock(item.getStock() + qtyDelta);
        boolean isLowNow = item.getStock() <= item.getThreshold();
        item.setLowStockNotified(isLowNow);

        InventoryItem saved = inventoryItemRepository.save(item);
        inventoryActivityService.logStockChange(saved, qtyDelta);

        if (isLowNow && !alreadyFlagged) {
            notificationService.createLowStockNotification(saved);
            inventoryActivityService.logLowStock(saved);
        } else if (!isLowNow && alreadyFlagged) {
            notificationService.resolveLowStockNotifications(saved.getId());
        }
        return saved;
    }

    public void delete(String id) {
        InventoryItem item = getById(id);
        inventoryItemRepository.deleteById(id);
        notificationService.resolveLowStockNotifications(id);
        inventoryActivityService.logDeleted(item);

        auditLogService.record("Inventory", "Inventory Item Deleted", id,
                item.getName() + " removed from inventory (was " + item.getStock() + " units @ ₹" + item.getPrice() + ")",
                "Stock: " + item.getStock() + ", Price: ₹" + item.getPrice(),
                null);
    }
}
