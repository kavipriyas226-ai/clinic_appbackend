package com.devsclinic.backend.service;

import com.devsclinic.backend.model.AuditLog;
import com.devsclinic.backend.repository.AuditLogRepository;
import com.devsclinic.backend.util.SequentialIdGenerator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Writes audit-trail entries for financial/inventory mutations (invoice creation and
 * cancellation, payment changes, inventory item changes) as they happen, capturing who made
 * the change from the request's JWT-derived authentication. This only covers changes made
 * from the point this service was introduced onward — there is no historical change log to
 * backfill from, so older records simply have no audit history.
 */
@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(String module, String action, String recordId, String summary, String oldValue, String newValue) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = auth != null ? auth.getName() : "system";
        String userRole = (auth != null && !auth.getAuthorities().isEmpty())
                ? auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")
                : "SYSTEM";

        List<String> existingIds = auditLogRepository.findAll().stream().map(AuditLog::getId).toList();
        String newId = SequentialIdGenerator.next(existingIds, "AUD-", 1);

        auditLogRepository.save(AuditLog.builder()
                .id(newId)
                .module(module)
                .action(action)
                .recordId(recordId)
                .summary(summary)
                .oldValue(oldValue)
                .newValue(newValue)
                .userName(userName)
                .userRole(userRole)
                .timestamp(Instant.now())
                .build());
    }

    /**
     * @param module case-insensitive exact match, or null/blank for all modules
     * @param from   inclusive local date, or null for no lower bound
     * @param to     inclusive local date, or null for no upper bound
     */
    public List<AuditLog> search(String module, String from, String to) {
        LocalDate fromDate = parseOrNull(from);
        LocalDate toDate = parseOrNull(to);

        return auditLogRepository.findAll().stream()
                .filter(log -> module == null || module.isBlank() || module.equalsIgnoreCase(log.getModule()))
                .filter(log -> isWithinRange(log.getTimestamp(), fromDate, toDate))
                .sorted(Comparator.comparing(AuditLog::getTimestamp).reversed())
                .toList();
    }

    private boolean isWithinRange(Instant timestamp, LocalDate from, LocalDate to) {
        if (from == null && to == null) return true;
        LocalDate date = timestamp.atZone(ZoneId.systemDefault()).toLocalDate();
        if (from != null && date.isBefore(from)) return false;
        if (to != null && date.isAfter(to)) return false;
        return true;
    }

    private LocalDate parseOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
    }
}
