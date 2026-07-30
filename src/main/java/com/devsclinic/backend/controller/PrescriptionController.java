package com.devsclinic.backend.controller;

import com.devsclinic.backend.dto.PrescriptionRequest;
import com.devsclinic.backend.model.Prescription;
import com.devsclinic.backend.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @GetMapping
    public List<Prescription> getByPatient(@RequestParam String patientId) {
        return prescriptionService.getByPatient(patientId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Prescription create(@Valid @RequestBody PrescriptionRequest request) {
        return prescriptionService.create(request);
    }
}
