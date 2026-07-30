package com.devsclinic.backend.repository;

import com.devsclinic.backend.model.Account;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccountRepository extends MongoRepository<Account, String> {
}
