package com.devsclinic.backend.service;

import com.devsclinic.backend.dto.PaymentsSummaryResponse;
import com.devsclinic.backend.model.InstallmentPayment;
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

    /** "Total Collected" sums actual installment payments made within the period (by
     * payment date, not invoice date) — money genuinely received. "Total Outstanding"
     * is the current unpaid balance across every invoice, a snapshot rather than a
     * period-filtered figure. */
    public PaymentsSummaryResponse getSummary(String period) {
        LocalDate today = LocalDate.now();

        double totalCollected = invoiceRepository.findAll().stream()
                .flatMap(inv -> inv.getPayments().stream())
                .filter(payment -> isWithinPeriod(payment.getDate(), period, today))
                .mapToDouble(InstallmentPayment::getAmount)
                .sum();

        double totalUnpaid = invoiceRepository.findAll().stream()
                .mapToDouble(Invoice::getBalance)
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
