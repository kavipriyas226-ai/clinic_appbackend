package com.devsclinic.backend.repository;

import com.devsclinic.backend.model.ClinicProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClinicProfileRepository extends MongoRepository<ClinicProfile, String> {
}
