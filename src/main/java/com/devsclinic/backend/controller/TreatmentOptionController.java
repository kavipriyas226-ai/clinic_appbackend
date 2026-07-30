package com.devsclinic.backend.controller;

import com.devsclinic.backend.dto.TreatmentOptionRequest;
import com.devsclinic.backend.model.TreatmentOption;
import com.devsclinic.backend.service.TreatmentOptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/treatments")
public class TreatmentOptionController {

    private final TreatmentOptionService treatmentOptionService;

    public TreatmentOptionController(TreatmentOptionService treatmentOptionService) {
        this.treatmentOptionService = treatmentOptionService;
    }

    @GetMapping
    public List<TreatmentOption> getAll() {
        return treatmentOptionService.getAll();
    }

    @GetMapping("/{id}")
    public TreatmentOption getById(@PathVariable String id) {
        return treatmentOptionService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TreatmentOption create(@Valid @RequestBody TreatmentOptionRequest request) {
        return treatmentOptionService.create(request);
    }

    @PutMapping("/{id}")
    public TreatmentOption update(@PathVariable String id, @Valid @RequestBody TreatmentOptionRequest request) {
        return treatmentOptionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        treatmentOptionService.delete(id);
    }
}
