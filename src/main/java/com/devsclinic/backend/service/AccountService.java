package com.devsclinic.backend.service;

import com.devsclinic.backend.dto.AccountResponse;
import com.devsclinic.backend.dto.ChangeAccountRequest;
import com.devsclinic.backend.exception.BadRequestException;
import com.devsclinic.backend.exception.ResourceNotFoundException;
import com.devsclinic.backend.model.Account;
import com.devsclinic.backend.repository.AccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AccountResponse getAccount() {
        Account account = getSingleton();
        return new AccountResponse(account.getUsername());
    }

    public AccountResponse updateAccount(ChangeAccountRequest request) {
        Account account = getSingleton();

        boolean changingPassword = request.newPassword() != null && !request.newPassword().isBlank();

        if (changingPassword) {
            if (request.currentPassword() == null || !passwordEncoder.matches(request.currentPassword(), account.getPasswordHash())) {
                throw new BadRequestException("Current password is incorrect");
            }
            account.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }

        account.setUsername(request.username().trim());
        accountRepository.save(account);
        return new AccountResponse(account.getUsername());
    }

    private Account getSingleton() {
        return accountRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }
}
