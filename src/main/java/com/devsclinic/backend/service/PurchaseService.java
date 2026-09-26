package com.devsclinic.backend.service;

import com.devsclinic.backend.dto.PurchaseLineItemRequest;
import com.devsclinic.backend.dto.PurchaseRequest;
import com.devsclinic.backend.exception.ResourceNotFoundException;
import com.devsclinic.backend.model.InventoryItem;
import com.devsclinic.backend.model.Purchase;
import com.devsclinic.backend.model.PurchaseLineItem;
import com.devsclinic.backend.repository.InventoryItemRepository;
import com.devsclinic.backend.repository.PurchaseRepository;
import com.devsclinic.backend.util.SequentialIdGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PurchaseService {

    /** Same historical fallback Billing uses for a product that hasn't been given its own
     * GST% in Product Master yet. */
    private static final double DEFAULT_GST_PERCENT = 18.0;

    private final PurchaseRepository purchaseRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryService inventoryService;
    private final AuditLogService auditLogService;

    public PurchaseService(
            PurchaseRepository purchaseRepository,
            InventoryItemRepository inventoryItemRepository,
            InventoryService inventoryService,
            AuditLogService auditLogService
    ) {
        this.purchaseRepository = purchaseRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryService = inventoryService;
        this.auditLogService = auditLogService;
    }

    public List<Purchase> getAll() {
        return purchaseRepository.findAll();
    }

    public Purchase getById(String id) {
        return purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found: " + id));
    }

    /** Creates and persists a purchase, then increases each purchased product's stock by the
     * quantity bought — the same stock-change logging and low-stock handling a manual
     * Inventory edit would get. */
    public Purchase create(PurchaseRequest request) {
        List<PurchaseLineItem> lineItems = request.lineItems().stream()
                .map(this::toLineItem)
                .toList();

        double subtotal = lineItems.stream().mapToDouble(PurchaseLineItem::getTaxableAmount).sum();
        double gstAmount = lineItems.stream().mapToDouble(PurchaseLineItem::getGstAmount).sum();
        double total = subtotal + gstAmount;

        List<String> existingIds = purchaseRepository.findAll().stream().map(Purchase::getId).toList();
        String newId = SequentialIdGenerator.next(existingIds, "PUR-", 1001);
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        Purchase purchase = Purchase.builder()
                .id(newId)
                .supplierName(request.supplierName())
                .address(request.address())
                .gstin(request.gstin())
                .date(today)
                .lineItems(lineItems)
                .subtotal(subtotal)
                .gstAmount(gstAmount)
                .total(total)
                .build();

        Purchase saved = purchaseRepository.save(purchase);

        for (PurchaseLineItem item : lineItems) {
            inventoryService.addStock(item.getRefId(), item.getQty());
        }

        auditLogService.record("Purchase", "Purchase Recorded", saved.getId(),
                "Purchase " + saved.getId() + " from " + saved.getSupplierName()
                        + " — Total ₹" + saved.getTotal() + " (" + lineItems.size() + " item(s), stock updated)",
                null,
                "Total: ₹" + saved.getTotal() + ", Supplier: " + saved.getSupplierName());

        return saved;
    }

    /** Deletes a purchase and reverses its stock addition, so cancelling a mistaken purchase
     * doesn't leave inventory permanently inflated. */
    public void delete(String id) {
        Purchase purchase = getById(id);
        purchaseRepository.deleteById(id);

        for (PurchaseLineItem item : purchase.getLineItems()) {
            inventoryService.addStock(item.getRefId(), -item.getQty());
        }

        auditLogService.record("Purchase", "Purchase Deleted", id,
                "Purchase " + id + " from " + purchase.getSupplierName() + " was deleted (was Total ₹" + purchase.getTotal() + ") — stock reversed",
                "Total: ₹" + purchase.getTotal(),
                null);
    }

    /** Resolves GST%/HSN-SAC from Product Master and snapshots them onto the line item at
     * purchase time, since a later change to the product shouldn't rewrite an already-recorded
     * purchase. */
    private PurchaseLineItem toLineItem(PurchaseLineItemRequest r) {
        double gstPercent = DEFAULT_GST_PERCENT;
        String hsnSacCode = null;

        InventoryItem product = inventoryItemRepository.findById(r.refId()).orElse(null);
        if (product != null) {
            if (product.getGstPercent() != null) gstPercent = product.getGstPercent();
            hsnSacCode = product.getHsnSacCode();
        }

        double gstAmount = r.taxableAmount() * gstPercent / 100;

        return PurchaseLineItem.builder()
                .refId(r.refId())
                .name(r.name())
                .hsnSacCode(hsnSacCode)
                .gstPercent(gstPercent)
                .rate(r.rate())
                .qty(r.qty())
                .taxableAmount(r.taxableAmount())
                .gstAmount(gstAmount)
                .totalAmount(r.taxableAmount() + gstAmount)
                .build();
    }
}
