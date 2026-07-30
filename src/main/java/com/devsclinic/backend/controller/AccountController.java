package com.devsclinic.backend.controller;

import com.devsclinic.backend.dto.AccountResponse;
import com.devsclinic.backend.dto.ChangeAccountRequest;
import com.devsclinic.backend.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public AccountResponse getAccount() {
        return accountService.getAccount();
    }

    @PutMapping
    public AccountResponse updateAccount(@Valid @RequestBody ChangeAccountRequest request) {
        return accountService.updateAccount(request);
    }
}
