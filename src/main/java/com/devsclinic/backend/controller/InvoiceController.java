package com.devsclinic.backend.controller;

import com.devsclinic.backend.dto.InvoiceRequest;
import com.devsclinic.backend.dto.InvoiceUpdateRequest;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Invoice create(@Valid @RequestBody InvoiceRequest request) {
        return billingService.create(request);
    }

    @GetMapping("/summary")
    public PaymentsSummaryResponse getSummary(@RequestParam(defaultValue = "total") String period) {
        return paymentService.getSummary(period);
    }

    @PutMapping("/{id}")
    public Invoice update(@PathVariable String id, @Valid @RequestBody InvoiceUpdateRequest request) {
        return billingService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        billingService.delete(id);
    }
}
