package com.devsclinic.backend.service;

import com.devsclinic.backend.dto.InvoiceRequest;
import com.devsclinic.backend.dto.LineItemRequest;
import com.devsclinic.backend.exception.ResourceNotFoundException;
import com.devsclinic.backend.model.*;
import com.devsclinic.backend.repository.InvoiceRepository;
import com.devsclinic.backend.repository.PatientRepository;
import com.devsclinic.backend.util.SequentialIdGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BillingService {

    private static final double GST_RATE = 0.18;

    private final InvoiceRepository invoiceRepository;
    private final PatientRepository patientRepository;

    public BillingService(InvoiceRepository invoiceRepository, PatientRepository patientRepository) {
        this.invoiceRepository = invoiceRepository;
        this.patientRepository = patientRepository;
    }

    public List<Invoice> getAll() {
        return invoiceRepository.findAll();
    }

    public Invoice getById(String id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
    }

    /**
     * Creates and persists an invoice, mirroring Billing.jsx's subtotal/discount/GST math
     * exactly. The clinic desk finalizes payment at print time, so new invoices are
     * recorded as Paid via the default in-clinic method; there's no separate "mark as
     * paid" step anywhere in the UI.
     */
    public Invoice create(InvoiceRequest request) {
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + request.patientId()));

        List<LineItem> lineItems = request.lineItems().stream()
                .map(this::toLineItem)
                .toList();

        double subtotal = lineItems.stream().mapToDouble(LineItem::getAmount).sum();
        double discountAmount = request.discountEnabled() ? subtotal * request.discountPercent() / 100 : 0;
        double taxable = subtotal - discountAmount;
        double gstAmount = request.gstEnabled() ? taxable * GST_RATE : 0;
        double total = taxable + gstAmount;

        List<String> existingIds = invoiceRepository.findAll().stream().map(Invoice::getId).toList();
        String newId = SequentialIdGenerator.next(existingIds, "INV-", 3001);
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        Invoice invoice = Invoice.builder()
                .id(newId)
                .patientId(patient.getId())
                .patientName(patient.getName())
                .date(today)
                .lineItems(lineItems)
                .discountEnabled(request.discountEnabled())
                .discountPercent(request.discountPercent())
                .gstEnabled(request.gstEnabled())
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .gstAmount(gstAmount)
                .total(total)
                .status("Paid")
                .method("UPI")
                .build();

        Invoice saved = invoiceRepository.save(invoice);

        patient.getInvoices().add(PatientInvoiceSummary.builder()
                .id(saved.getId())
                .date(saved.getDate())
                .amount(saved.getTotal())
                .status(saved.getStatus())
                .build());
        patientRepository.save(patient);

        return saved;
    }

    private LineItem toLineItem(LineItemRequest r) {
        return LineItem.builder()
                .refId(r.refId())
                .type(r.type())
                .name(r.name())
                .price(r.price())
                .qty(r.qty())
                .amount(r.amount())
                .build();
    }
}
