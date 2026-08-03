package com.devsclinic.backend.service;

import com.devsclinic.backend.dto.CreateUserRequest;
import com.devsclinic.backend.dto.UpdateUserRequest;
import com.devsclinic.backend.dto.UserResponse;
import com.devsclinic.backend.exception.BadRequestException;
import com.devsclinic.backend.exception.ResourceNotFoundException;
import com.devsclinic.backend.model.Account;
import com.devsclinic.backend.repository.AccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private static final Set<String> VALID_ROLES = Set.of("ADMIN", "USER");

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getAll() {
        return accountRepository.findAllByOrderByRoleAscUsernameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse create(CreateUserRequest request) {
        String role = normalizeRole(request.role());
        String username = request.username().trim();

        if (accountRepository.findByUsernameIgnoreCase(username).isPresent()) {
            throw new BadRequestException("An account with this username already exists");
        }

        Account account = Account.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(role)
                .enabled(request.enabled())
                .build();

        return toResponse(accountRepository.save(account));
    }

    public UserResponse update(String id, UpdateUserRequest request) {
        Account account = getById(id);
        String newRole = normalizeRole(request.role());
        String username = request.username().trim();

        accountRepository.findByUsernameIgnoreCase(username).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BadRequestException("An account with this username already exists");
            }
        });

        boolean losingAdminCoverage = "ADMIN".equals(account.getRole())
                && (!"ADMIN".equals(newRole) || !request.enabled());
        if (losingAdminCoverage && otherEnabledAdminCount(id) == 0) {
            throw new BadRequestException("At least one enabled Admin account must remain");
        }

        account.setUsername(username);
        account.setRole(newRole);
        account.setEnabled(request.enabled());

        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            if (request.newPassword().length() < 6) {
                throw new BadRequestException("Password must be at least 6 characters");
            }
            account.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }

        return toResponse(accountRepository.save(account));
    }

    public void delete(String id) {
        Account account = getById(id);
        if ("ADMIN".equals(account.getRole()) && otherEnabledAdminCount(id) == 0) {
            throw new BadRequestException("At least one enabled Admin account must remain");
        }
        accountRepository.deleteById(id);
    }

    private long otherEnabledAdminCount(String excludingId) {
        return accountRepository.findAllByOrderByRoleAscUsernameAsc().stream()
                .filter(a -> !a.getId().equals(excludingId))
                .filter(a -> "ADMIN".equals(a.getRole()) && a.isEnabled())
                .count();
    }

    private Account getById(String id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private String normalizeRole(String role) {
        String upper = role == null ? "" : role.trim().toUpperCase();
        if (!VALID_ROLES.contains(upper)) {
            throw new BadRequestException("Role must be either ADMIN or USER");
        }
        return upper;
    }

    private UserResponse toResponse(Account account) {
        return new UserResponse(account.getId(), account.getUsername(), account.getRole(), account.isEnabled());
    }
}
