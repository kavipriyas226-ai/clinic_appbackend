package com.devsclinic.backend.controller;

import com.devsclinic.backend.dto.ClinicProfileRequest;
import com.devsclinic.backend.model.ClinicProfile;
import com.devsclinic.backend.service.ClinicProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clinic-profile")
public class ClinicProfileController {

    private final ClinicProfileService clinicProfileService;

    public ClinicProfileController(ClinicProfileService clinicProfileService) {
        this.clinicProfileService = clinicProfileService;
    }

    @GetMapping
    public ClinicProfile getProfile() {
        return clinicProfileService.getProfile();
    }

    @PutMapping
    public ClinicProfile updateProfile(@Valid @RequestBody ClinicProfileRequest request) {
        return clinicProfileService.updateProfile(request);
    }
}
