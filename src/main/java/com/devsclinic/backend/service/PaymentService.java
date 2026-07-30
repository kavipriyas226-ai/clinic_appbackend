package com.devsclinic.backend.service;

import com.devsclinic.backend.dto.PaymentsSummaryResponse;
import com.devsclinic.backend.model.Invoice;
import com.devsclinic.backend.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class PaymentService {

    private final InvoiceRepository invoiceRepository;

    public PaymentService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /** Mirrors Payments.jsx's isWithinPeriod() filtering exactly. */
    public PaymentsSummaryResponse getSummary(String period) {
        LocalDate today = LocalDate.now();

        double totalCollected = invoiceRepository.findAll().stream()
                .filter(inv -> "Paid".equals(inv.getStatus()))
                .filter(inv -> isWithinPeriod(inv.getDate(), period, today))
                .mapToDouble(Invoice::getTotal)
                .sum();

        double totalUnpaid = invoiceRepository.findAll().stream()
                .filter(inv -> "Unpaid".equals(inv.getStatus()))
                .mapToDouble(Invoice::getTotal)
                .sum();

        return new PaymentsSummaryResponse(totalCollected, totalUnpaid);
    }

    private boolean isWithinPeriod(String dateStr, String period, LocalDate today) {
        if (period == null || period.equals("total")) return true;
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (Exception e) {
            return false;
        }
        return switch (period) {
            case "today" -> date.isEqual(today);
            case "month" -> date.getYear() == today.getYear() && date.getMonth() == today.getMonth();
            case "year" -> date.getYear() == today.getYear();
            default -> true;
        };
    }
}
