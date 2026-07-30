package com.devsclinic.backend.repository;

import com.devsclinic.backend.model.TreatmentOption;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TreatmentOptionRepository extends MongoRepository<TreatmentOption, String> {
}
