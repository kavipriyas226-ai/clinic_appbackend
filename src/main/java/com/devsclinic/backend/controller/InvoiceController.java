package com.devsclinic.backend.controller;

import com.devsclinic.backend.dto.InstallmentPaymentRequest;
import com.devsclinic.backend.dto.InvoiceRequest;
import com.devsclinic.backend.dto.PaymentsSummaryResponse;
import com.devsclinic.backend.model.Invoice;
import com.devsclinic.backend.service.BillingService;
import com.devsclinic.backend.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final BillingService billingService;
    private final PaymentService paymentService;

    public InvoiceController(BillingService billingService, PaymentService paymentService) {
        this.billingService = billingService;
        this.paymentService = paymentService;
    }

    @GetMapping
    public List<Invoice> getAll() {
        return billingService.getAll();
    }

    @GetMapping("/{id}")
    public Invoice getById(@PathVariable String id) {
        return billingService.getById(id);
    }

    @GetMapping("/patient/{patientId}")
    public List<Invoice> getByPatient(@PathVariable String patientId) {
        return billingService.getByPatientId(patientId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Invoice create(@Valid @RequestBody InvoiceRequest request) {
        return billingService.create(request);
    }

    @GetMapping("/summary")
    public PaymentsSummaryResponse getSummary(@RequestParam(defaultValue = "total") String period) {
        return paymentService.getSummary(period);
    }

    @PostMapping("/{id}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public Invoice addPayment(@PathVariable String id, @Valid @RequestBody InstallmentPaymentRequest request) {
        return billingService.addPayment(id, request);
    }

    @PutMapping("/{id}/payments/{paymentId}")
    public Invoice updatePayment(
            @PathVariable String id,
            @PathVariable String paymentId,
            @Valid @RequestBody InstallmentPaymentRequest request
    ) {
        return billingService.updatePayment(id, paymentId, request);
    }

    @DeleteMapping("/{id}/payments/{paymentId}")
    public Invoice deletePayment(@PathVariable String id, @PathVariable String paymentId) {
        return billingService.deletePayment(id, paymentId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        billingService.delete(id);
    }
}
