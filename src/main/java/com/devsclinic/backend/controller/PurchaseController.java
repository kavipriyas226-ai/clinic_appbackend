package com.devsclinic.backend.controller;

import com.devsclinic.backend.dto.PurchaseRequest;
import com.devsclinic.backend.model.Purchase;
import com.devsclinic.backend.service.PurchaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping
    public List<Purchase> getAll() {
        return purchaseService.getAll();
    }

    @GetMapping("/{id}")
    public Purchase getById(@PathVariable String id) {
        return purchaseService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Purchase create(@Valid @RequestBody PurchaseRequest request) {
        return purchaseService.create(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        purchaseService.delete(id);
    }
}
