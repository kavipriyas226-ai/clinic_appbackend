package com.devsclinic.backend.repository;

import com.devsclinic.backend.model.Account;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends MongoRepository<Account, String> {
    Optional<Account> findByUsernameIgnoreCase(String username);
    long countByRole(String role);
    List<Account> findAllByOrderByRoleAscUsernameAsc();
}
