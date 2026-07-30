package com.devsclinic.backend.controller;

import com.devsclinic.backend.dto.PatientCreateRequest;
import com.devsclinic.backend.dto.PatientUpdateRequest;
import com.devsclinic.backend.model.Patient;
import com.devsclinic.backend.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public List<Patient> getAll() {
        return patientService.getAll();
    }

    @GetMapping("/{id}")
    public Patient getById(@PathVariable String id) {
        return patientService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Patient create(@Valid @RequestBody PatientCreateRequest request) {
        return patientService.create(request);
    }

    @PutMapping("/{id}")
    public Patient update(@PathVariable String id, @Valid @RequestBody PatientUpdateRequest request) {
        return patientService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        patientService.delete(id);
    }
}
