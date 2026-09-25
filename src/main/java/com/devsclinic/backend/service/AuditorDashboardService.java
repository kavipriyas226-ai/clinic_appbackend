package com.devsclinic.backend.service;

import com.devsclinic.backend.dto.AuditorDashboardResponse;
import com.devsclinic.backend.model.Invoice;
import com.devsclinic.backend.repository.InventoryItemRepository;
import com.devsclinic.backend.repository.InvoiceRepository;
import com.devsclinic.backend.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AuditorDashboardService {

    private final InvoiceRepository invoiceRepository;
    private final PatientRepository patientRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public AuditorDashboardService(
            InvoiceRepository invoiceRepository,
            PatientRepository patientRepository,
            InventoryItemRepository inventoryItemRepository
    ) {
        this.invoiceRepository = invoiceRepository;
        this.patientRepository = patientRepository;
        this.inventoryItemRepository = inventoryItemRepository;
    }

    /**
     * @param from inclusive ISO date (yyyy-MM-dd), or null for no lower bound
     * @param to   inclusive ISO date (yyyy-MM-dd), or null for no upper bound
     */
    public AuditorDashboardResponse getStats(String from, String to) {
        LocalDate fromDate = parseOrNull(from);
        LocalDate toDate = parseOrNull(to);

        List<Invoice> invoicesInRange = invoiceRepository.findAll().stream()
                .filter(inv -> isWithinRange(inv.getDate(), fromDate, toDate))
                .toList();

        double totalBillingAmount = invoicesInRange.stream().mapToDouble(Invoice::getTotal).sum();
        double totalCollected = invoicesInRange.stream().mapToDouble(Invoice::getAmountPaid).sum();
        double totalPending = invoicesInRange.stream().mapToDouble(Invoice::getBalance).sum();
        double totalDiscounts = invoicesInRange.stream().mapToDouble(Invoice::getDiscountAmount).sum();
        // No separate CGST/SGST field exists on Invoice — only a single combined gstAmount.
        // Tamil Nadu intra-state billing splits GST evenly, matching exactly how the Billing
        // module itself derives CGST/SGST for display (gstAmount / 2 each); this is the same
        // math, not a new/independent calculation.
        double totalGst = invoicesInRange.stream().mapToDouble(Invoice::getGstAmount).sum();
        double totalCgst = totalGst / 2;
        double totalSgst = totalGst / 2;
        int totalBillingTransactions = invoicesInRange.stream().mapToInt(inv -> inv.getLineItems().size()).sum();

        double inventoryValue = inventoryItemRepository.findAll().stream()
                .mapToDouble(item -> item.getPrice() * item.getStock())
                .sum();

        return new AuditorDashboardResponse(
                totalBillingAmount,
                totalCollected,
                totalPending,
                totalDiscounts,
                totalCgst,
                totalSgst,
                inventoryValue,
                invoicesInRange.size(),
                (int) patientRepository.count(),
                totalBillingTransactions
        );
    }

    private boolean isWithinRange(String dateStr, LocalDate from, LocalDate to) {
        if (from == null && to == null) return true;
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (Exception e) {
            return false;
        }
        if (from != null && date.isBefore(from)) return false;
        if (to != null && date.isAfter(to)) return false;
        return true;
    }

    private LocalDate parseOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
    }
}
