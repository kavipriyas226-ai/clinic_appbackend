package com.devsclinic.backend.service;

import com.devsclinic.backend.dto.LoginRequest;
import com.devsclinic.backend.dto.LoginResponse;
import com.devsclinic.backend.model.Account;
import com.devsclinic.backend.repository.AccountRepository;
import com.devsclinic.backend.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AccountRepository accountRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Account account = accountRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!account.getUsername().equalsIgnoreCase(request.username())
                || !passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String token = jwtService.generateToken(account.getUsername());
        return new LoginResponse(token, account.getUsername());
    }
}
