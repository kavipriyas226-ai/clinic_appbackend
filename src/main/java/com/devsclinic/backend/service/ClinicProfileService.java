package com.devsclinic.backend.service;

import com.devsclinic.backend.dto.ClinicProfileRequest;
import com.devsclinic.backend.exception.ResourceNotFoundException;
import com.devsclinic.backend.model.ClinicProfile;
import com.devsclinic.backend.repository.ClinicProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class ClinicProfileService {

    private final ClinicProfileRepository clinicProfileRepository;

    public ClinicProfileService(ClinicProfileRepository clinicProfileRepository) {
        this.clinicProfileRepository = clinicProfileRepository;
    }

    public ClinicProfile getProfile() {
        return clinicProfileRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Clinic profile not found"));
    }

    public ClinicProfile updateProfile(ClinicProfileRequest request) {
        ClinicProfile profile = getProfile();
        profile.setName(request.name());
        profile.setTagline(request.tagline());
        profile.setPhone(request.phone());
        profile.setEmail(request.email());
        profile.setGstin(request.gstin());
        profile.setAddress(request.address());
        if (request.logoDataUrl() != null) {
            profile.setLogoDataUrl(request.logoDataUrl());
        }
        return clinicProfileRepository.save(profile);
    }
}
