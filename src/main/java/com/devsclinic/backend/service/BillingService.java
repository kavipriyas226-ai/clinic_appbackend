package com.devsclinic.backend.service;

import com.devsclinic.backend.dto.InstallmentPaymentRequest;
import com.devsclinic.backend.dto.InvoiceRequest;
import com.devsclinic.backend.dto.LineItemRequest;
import com.devsclinic.backend.exception.BadRequestException;
import com.devsclinic.backend.exception.ResourceNotFoundException;
import com.devsclinic.backend.model.*;
import com.devsclinic.backend.repository.InvoiceRepository;
import com.devsclinic.backend.repository.PatientRepository;
import com.devsclinic.backend.util.SequentialIdGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    public List<Invoice> getByPatientId(String patientId) {
        return invoiceRepository.findAllByPatientIdOrderByDateDesc(patientId);
    }

    /**
     * Creates and persists an invoice. The invoice's {@code total} is the full treatment
     * amount owed; {@code initialPaymentAmount} (typically what the patient paid on this
     * first visit) may be less than the total, in which case the remainder becomes the
     * invoice's balance and is expected to be collected via {@link #addPayment} on later
     * visits.
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
                .payments(new ArrayList<>())
                .build();

        if (request.initialPaymentAmount() > 0) {
            invoice.getPayments().add(InstallmentPayment.builder()
                    .id("PAY-1")
                    .amount(request.initialPaymentAmount())
                    .method(request.initialPaymentMethod() != null && !request.initialPaymentMethod().isBlank()
                            ? request.initialPaymentMethod() : "UPI")
                    .date(today)
                    .note("1st Visit")
                    .build());
        }
        recomputePaymentState(invoice);

        Invoice saved = invoiceRepository.save(invoice);

        patient.getInvoices().add(toSummary(saved));
        patientRepository.save(patient);

        return saved;
    }

    /** Records a new installment payment (a visit's payment) against an existing invoice. */
    public Invoice addPayment(String invoiceId, InstallmentPaymentRequest request) {
        Invoice invoice = getById(invoiceId);
        if (invoice.getBalance() <= 0) {
            throw new BadRequestException("This invoice is already fully paid.");
        }

        List<String> existingIds = invoice.getPayments().stream().map(InstallmentPayment::getId).toList();
        String newId = SequentialIdGenerator.next(existingIds, "PAY-", 1);

        invoice.getPayments().add(InstallmentPayment.builder()
                .id(newId)
                .amount(request.amount())
                .method(request.method())
                .date(request.date())
                .note(request.note())
                .build());

        return saveAndSync(invoice);
    }

    /** Edits a previously recorded installment (e.g. fixing the amount or method). */
    public Invoice updatePayment(String invoiceId, String paymentId, InstallmentPaymentRequest request) {
        Invoice invoice = getById(invoiceId);
        InstallmentPayment payment = invoice.getPayments().stream()
                .filter(p -> p.getId().equals(paymentId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));

        payment.setAmount(request.amount());
        payment.setMethod(request.method());
        payment.setDate(request.date());
        payment.setNote(request.note());

        return saveAndSync(invoice);
    }

    public Invoice deletePayment(String invoiceId, String paymentId) {
        Invoice invoice = getById(invoiceId);
        boolean removed = invoice.getPayments().removeIf(p -> p.getId().equals(paymentId));
        if (!removed) {
            throw new ResourceNotFoundException("Payment not found: " + paymentId);
        }
        return saveAndSync(invoice);
    }

    public void delete(String id) {
        Invoice invoice = getById(id);
        invoiceRepository.deleteById(id);

        patientRepository.findById(invoice.getPatientId()).ifPresent(patient -> {
            patient.getInvoices().removeIf(summary -> summary.getId().equals(id));
            patientRepository.save(patient);
        });
    }

    /** One-time (per invoice) backfill: gives every pre-existing invoice a real payment
     * ledger so old data works with the new installment tracking. Safe to run on every
     * startup — invoices that already have a payments list are skipped. */
    public void migrateLegacyPayments() {
        List<Invoice> toMigrate = invoiceRepository.findAll().stream()
                .filter(inv -> inv.getPayments() == null || inv.getPayments().isEmpty())
                .filter(inv -> inv.getTotal() > 0)
                .toList();
        if (toMigrate.isEmpty()) return;

        for (Invoice invoice : toMigrate) {
            List<InstallmentPayment> payments = new ArrayList<>();
            if ("Paid".equalsIgnoreCase(invoice.getStatus()) || "Fully Paid".equalsIgnoreCase(invoice.getStatus())) {
                payments.add(InstallmentPayment.builder()
                        .id("PAY-1")
                        .amount(invoice.getTotal())
                        .method(invoice.getMethod() != null && !invoice.getMethod().isBlank() ? invoice.getMethod() : "—")
                        .date(invoice.getDate())
                        .note("Full payment")
                        .build());
            }
            invoice.setPayments(payments);
            recomputePaymentState(invoice);
        }
        invoiceRepository.saveAll(toMigrate);

        toMigrate.forEach(invoice -> patientRepository.findById(invoice.getPatientId()).ifPresent(patient -> {
            patient.getInvoices().stream()
                    .filter(s -> s.getId().equals(invoice.getId()))
                    .findFirst()
                    .ifPresentOrElse(
                            s -> {
                                s.setAmountPaid(invoice.getAmountPaid());
                                s.setBalance(invoice.getBalance());
                                s.setStatus(invoice.getStatus());
                            },
                            () -> patient.getInvoices().add(toSummary(invoice))
                    );
            patientRepository.save(patient);
        }));
    }

    private Invoice saveAndSync(Invoice invoice) {
        recomputePaymentState(invoice);
        Invoice saved = invoiceRepository.save(invoice);

        patientRepository.findById(saved.getPatientId()).ifPresent(patient -> {
            patient.getInvoices().stream()
                    .filter(s -> s.getId().equals(saved.getId()))
                    .findFirst()
                    .ifPresent(s -> {
                        s.setAmountPaid(saved.getAmountPaid());
                        s.setBalance(saved.getBalance());
                        s.setStatus(saved.getStatus());
                    });
            patientRepository.save(patient);
        });

        return saved;
    }

    private void recomputePaymentState(Invoice invoice) {
        double amountPaid = invoice.getPayments().stream().mapToDouble(InstallmentPayment::getAmount).sum();
        double balance = Math.max(0, invoice.getTotal() - amountPaid);
        String status = amountPaid <= 0 ? "Pending" : (balance <= 0 ? "Fully Paid" : "Partially Paid");
        String method = invoice.getPayments().isEmpty()
                ? "—"
                : invoice.getPayments().get(invoice.getPayments().size() - 1).getMethod();

        invoice.setAmountPaid(amountPaid);
        invoice.setBalance(balance);
        invoice.setStatus(status);
        invoice.setMethod(method);
    }

    private PatientInvoiceSummary toSummary(Invoice invoice) {
        return PatientInvoiceSummary.builder()
                .id(invoice.getId())
                .date(invoice.getDate())
                .amount(invoice.getTotal())
                .amountPaid(invoice.getAmountPaid())
                .balance(invoice.getBalance())
                .status(invoice.getStatus())
                .build();
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
