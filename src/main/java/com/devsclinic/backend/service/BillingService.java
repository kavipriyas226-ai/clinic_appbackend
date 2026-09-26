package com.devsclinic.backend.service;

import com.devsclinic.backend.dto.InstallmentPaymentRequest;
import com.devsclinic.backend.dto.InvoiceRequest;
import com.devsclinic.backend.dto.LineItemRequest;
import com.devsclinic.backend.exception.BadRequestException;
import com.devsclinic.backend.exception.ResourceNotFoundException;
import com.devsclinic.backend.model.*;
import com.devsclinic.backend.repository.InventoryItemRepository;
import com.devsclinic.backend.repository.InvoiceRepository;
import com.devsclinic.backend.repository.PatientRepository;
import com.devsclinic.backend.util.SequentialIdGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BillingService {

    /** Flat GST rate used whenever a line's product hasn't been given its own GST% in Product
     * Master yet — this is the rate every invoice used before per-item rates existed, so
     * pre-existing/not-yet-configured products keep billing exactly as before. */
    private static final double DEFAULT_GST_PERCENT = 18.0;

    private final InvoiceRepository invoiceRepository;
    private final PatientRepository patientRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final AuditLogService auditLogService;

    public BillingService(
            InvoiceRepository invoiceRepository,
            PatientRepository patientRepository,
            InventoryItemRepository inventoryItemRepository,
            AuditLogService auditLogService
    ) {
        this.invoiceRepository = invoiceRepository;
        this.patientRepository = patientRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.auditLogService = auditLogService;
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
        // Applies the flat invoice-level discount proportionally to every line, so each line's
        // own GST rate is charged on its fair share of the discounted taxable amount rather
        // than on its full pre-discount amount.
        double discountRatio = subtotal > 0 ? (subtotal - discountAmount) / subtotal : 1;
        for (LineItem item : lineItems) {
            double lineTaxable = item.getAmount() * discountRatio;
            double lineGstAmount = request.gstEnabled() ? lineTaxable * item.getGstPercent() / 100 : 0;
            item.setGstAmount(lineGstAmount);
            item.setTotalAmount(lineTaxable + lineGstAmount);
        }

        double taxable = subtotal - discountAmount;
        double gstAmount = lineItems.stream().mapToDouble(LineItem::getGstAmount).sum();
        double total = taxable + gstAmount;

        List<String> existingIds = invoiceRepository.findAll().stream().map(Invoice::getId).toList();
        String newId = SequentialIdGenerator.next(existingIds, "INV-", 3001);
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        Invoice invoice = Invoice.builder()
                .id(newId)
                .patientId(patient.getId())
                .patientName(patient.getName())
                .date(today)
                .partyAddress(request.partyAddress())
                .partyGstin(request.partyGstin())
                .partyState(request.partyState())
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
                    .visitNumber(nextVisitNumber(patient.getId()))
                    .build());
        }
        recomputePaymentState(invoice);

        Invoice saved = invoiceRepository.save(invoice);

        patient.getInvoices().add(toSummary(saved));
        patientRepository.save(patient);

        auditLogService.record("Billing", "Invoice Created", saved.getId(),
                "Invoice " + saved.getId() + " created for " + saved.getPatientName()
                        + " — Total ₹" + saved.getTotal() + (saved.isGstEnabled() ? " (GST enabled)" : " (GST disabled)"),
                null,
                "Total: ₹" + saved.getTotal() + ", GST: " + (saved.isGstEnabled() ? "Enabled" : "Disabled") + ", Status: " + saved.getStatus());

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
                .visitNumber(nextVisitNumber(invoice.getPatientId()))
                .build());

        Invoice saved = saveAndSync(invoice);

        auditLogService.record("Payment", "Payment Added", invoiceId,
                "Recorded a payment of ₹" + request.amount() + " (" + request.method() + ") on invoice " + invoiceId,
                null,
                "Amount: ₹" + request.amount() + ", Method: " + request.method() + ", Date: " + request.date());

        return saved;
    }

    /** Edits a previously recorded installment (e.g. fixing the amount or method). */
    public Invoice updatePayment(String invoiceId, String paymentId, InstallmentPaymentRequest request) {
        Invoice invoice = getById(invoiceId);
        InstallmentPayment payment = invoice.getPayments().stream()
                .filter(p -> p.getId().equals(paymentId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));

        String oldValue = "Amount: ₹" + payment.getAmount() + ", Method: " + payment.getMethod() + ", Date: " + payment.getDate();

        payment.setAmount(request.amount());
        payment.setMethod(request.method());
        payment.setDate(request.date());
        payment.setNote(request.note());

        Invoice saved = saveAndSync(invoice);

        auditLogService.record("Payment", "Payment Updated", invoiceId,
                "Edited payment " + paymentId + " on invoice " + invoiceId,
                oldValue,
                "Amount: ₹" + request.amount() + ", Method: " + request.method() + ", Date: " + request.date());

        return saved;
    }

    public Invoice deletePayment(String invoiceId, String paymentId) {
        Invoice invoice = getById(invoiceId);
        InstallmentPayment removed = invoice.getPayments().stream()
                .filter(p -> p.getId().equals(paymentId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));
        invoice.getPayments().remove(removed);

        Invoice saved = saveAndSync(invoice);

        auditLogService.record("Payment", "Payment Deleted", invoiceId,
                "Removed a payment of ₹" + removed.getAmount() + " (" + removed.getMethod() + ") from invoice " + invoiceId,
                "Amount: ₹" + removed.getAmount() + ", Method: " + removed.getMethod() + ", Date: " + removed.getDate(),
                null);

        return saved;
    }

    public void delete(String id) {
        Invoice invoice = getById(id);
        invoiceRepository.deleteById(id);

        patientRepository.findById(invoice.getPatientId()).ifPresent(patient -> {
            patient.getInvoices().removeIf(summary -> summary.getId().equals(id));
            patientRepository.save(patient);
        });

        auditLogService.record("Billing", "Invoice Cancelled", id,
                "Invoice " + id + " for " + invoice.getPatientName() + " was deleted (was Total ₹" + invoice.getTotal() + ", " + invoice.getStatus() + ")",
                "Total: ₹" + invoice.getTotal() + ", Status: " + invoice.getStatus(),
                null);
    }

    /** One-time (per invoice) backfill: gives every pre-existing invoice a real payment
     * ledger so old data works with the new installment tracking. Safe to run on every
     * startup — invoices that already have a payments list are skipped. */
    public void migrateLegacyPayments() {
        List<Invoice> toMigrate = invoiceRepository.findAll().stream()
                .filter(inv -> inv.getPayments() == null || inv.getPayments().isEmpty())
                .filter(inv -> inv.getTotal() > 0)
                .sorted(Comparator.comparing(Invoice::getDate))
                .toList();
        if (toMigrate.isEmpty()) return;

        // Assigns visit numbers in chronological order per patient, seeded from whatever
        // that patient already has recorded elsewhere (invoices excluded above because they
        // already carry real payments), so a backfilled visit continues the same sequence a
        // live payment would have used.
        Map<String, Integer> nextVisitByPatient = new HashMap<>();
        for (Invoice invoice : toMigrate) {
            List<InstallmentPayment> payments = new ArrayList<>();
            if ("Paid".equalsIgnoreCase(invoice.getStatus()) || "Fully Paid".equalsIgnoreCase(invoice.getStatus())) {
                nextVisitByPatient.putIfAbsent(invoice.getPatientId(), nextVisitNumber(invoice.getPatientId()));
                int visitNumber = nextVisitByPatient.merge(invoice.getPatientId(), 1, Integer::sum) - 1;
                payments.add(InstallmentPayment.builder()
                        .id("PAY-1")
                        .amount(invoice.getTotal())
                        .method(invoice.getMethod() != null && !invoice.getMethod().isBlank() ? invoice.getMethod() : "—")
                        .date(invoice.getDate())
                        .note("Full payment")
                        .visitNumber(visitNumber)
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

    /** The next sequential visit number for a patient, counted across every payment already
     * recorded against any of their invoices — so a returning patient's next payment
     * continues their existing sequence instead of restarting at 1 for each new invoice. */
    private int nextVisitNumber(String patientId) {
        int paymentsSoFar = invoiceRepository.findAllByPatientIdOrderByDateDesc(patientId).stream()
                .mapToInt(inv -> inv.getPayments() == null ? 0 : inv.getPayments().size())
                .sum();
        return paymentsSoFar + 1;
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

    /** Resolves GST%/HSN-SAC from Product Master (Medicine lines only — Treatments don't yet
     * carry their own rate, so they use the historical flat default) and snapshots them onto
     * the line item at creation time, since a later change to the product shouldn't rewrite
     * an already-issued invoice. */
    private LineItem toLineItem(LineItemRequest r) {
        double gstPercent = DEFAULT_GST_PERCENT;
        String hsnSacCode = null;

        if ("Medicine".equalsIgnoreCase(r.type())) {
            InventoryItem product = inventoryItemRepository.findById(r.refId()).orElse(null);
            if (product != null) {
                if (product.getGstPercent() != null) gstPercent = product.getGstPercent();
                hsnSacCode = product.getHsnSacCode();
            }
        }

        return LineItem.builder()
                .refId(r.refId())
                .type(r.type())
                .name(r.name())
                .price(r.price())
                .qty(r.qty())
                .amount(r.amount())
                .gstPercent(gstPercent)
                .hsnSacCode(hsnSacCode)
                .build();
    }
}
