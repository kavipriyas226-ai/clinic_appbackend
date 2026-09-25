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
        Account account = accountRepository.findByUsernameIgnoreCase(request.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        if (!account.isEnabled()) {
            throw new BadCredentialsException("This account has been disabled. Contact your administrator.");
        }

        if (!account.getRole().equalsIgnoreCase(request.loginType())) {
            throw new BadCredentialsException("Please use \"" + displayLoginType(account.getRole()) + "\" to sign in with this account.");
        }

        String token = jwtService.generateToken(account.getUsername(), account.getRole());
        return new LoginResponse(token, account.getUsername(), account.getRole());
    }

    private String displayLoginType(String role) {
        if ("ADMIN".equalsIgnoreCase(role)) return "Admin Login";
        if ("AUDITOR".equalsIgnoreCase(role)) return "Auditor Login";
        return "Staff Login";
    }
}
