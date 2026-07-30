package com.devsclinic.backend.service;

import com.devsclinic.backend.dto.TreatmentOptionRequest;
import com.devsclinic.backend.exception.ResourceNotFoundException;
import com.devsclinic.backend.model.TreatmentOption;
import com.devsclinic.backend.repository.TreatmentOptionRepository;
import com.devsclinic.backend.util.SequentialIdGenerator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TreatmentOptionService {

    private final TreatmentOptionRepository treatmentOptionRepository;

    public TreatmentOptionService(TreatmentOptionRepository treatmentOptionRepository) {
        this.treatmentOptionRepository = treatmentOptionRepository;
    }

    public List<TreatmentOption> getAll() {
        return treatmentOptionRepository.findAll();
    }

    public TreatmentOption getById(String id) {
        return treatmentOptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Treatment not found: " + id));
    }

    public TreatmentOption create(TreatmentOptionRequest request) {
        List<String> existingIds = treatmentOptionRepository.findAll().stream().map(TreatmentOption::getId).toList();
        String newId = SequentialIdGenerator.next(existingIds, "TR-", 1);

        TreatmentOption treatment = TreatmentOption.builder()
                .id(newId)
                .name(request.name())
                .category(request.category())
                .price(request.price())
                .build();

        return treatmentOptionRepository.save(treatment);
    }

    public TreatmentOption update(String id, TreatmentOptionRequest request) {
        TreatmentOption treatment = getById(id);
        treatment.setName(request.name());
        treatment.setCategory(request.category());
        treatment.setPrice(request.price());
        return treatmentOptionRepository.save(treatment);
    }

    public void delete(String id) {
        if (!treatmentOptionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Treatment not found: " + id);
        }
        treatmentOptionRepository.deleteById(id);
    }
}
