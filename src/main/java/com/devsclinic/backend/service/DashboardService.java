package com.devsclinic.backend.service;

import com.devsclinic.backend.dto.DashboardStatsResponse;
import com.devsclinic.backend.repository.InventoryItemRepository;
import com.devsclinic.backend.repository.InvoiceRepository;
import com.devsclinic.backend.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DashboardService {

    private final PatientRepository patientRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InvoiceRepository invoiceRepository;

    public DashboardService(
            PatientRepository patientRepository,
            InventoryItemRepository inventoryItemRepository,
            InvoiceRepository invoiceRepository
    ) {
        this.patientRepository = patientRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public DashboardStatsResponse getStats() {
        LocalDate today = LocalDate.now();
        String todayStr = today.toString();

        int totalPatients = (int) patientRepository.count();

        int todaysPatients = (int) patientRepository.findAll().stream()
                .filter(p -> todayStr.equals(p.getLastVisit()))
                .count();

        int lowStockItems = (int) inventoryItemRepository.findAll().stream()
                .filter(i -> i.getStock() <= i.getThreshold())
                .count();

        double monthlyRevenue = invoiceRepository.findAll().stream()
                .filter(inv -> "Paid".equals(inv.getStatus()))
                .filter(inv -> {
                    try {
                        LocalDate date = LocalDate.parse(inv.getDate());
                        return date.getYear() == today.getYear() && date.getMonth() == today.getMonth();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .mapToDouble(inv -> inv.getTotal())
                .sum();

        return new DashboardStatsResponse(totalPatients, todaysPatients, monthlyRevenue, lowStockItems);
    }
}
