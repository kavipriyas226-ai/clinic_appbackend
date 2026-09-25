package com.devsclinic.backend.controller;

import com.devsclinic.backend.dto.AuditorDashboardResponse;
import com.devsclinic.backend.model.AuditLog;
import com.devsclinic.backend.service.AuditLogService;
import com.devsclinic.backend.service.AuditorDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Read-only reporting endpoints for the Auditor role. GET-only by design — SecurityConfig
 * denies every non-GET verb to AUDITOR globally, so this controller never needs its own
 * write endpoints or role checks beyond that. */
@RestController
@RequestMapping("/api/auditor")
public class AuditorController {

    private final AuditorDashboardService auditorDashboardService;
    private final AuditLogService auditLogService;

    public AuditorController(AuditorDashboardService auditorDashboardService, AuditLogService auditLogService) {
        this.auditorDashboardService = auditorDashboardService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/dashboard")
    public AuditorDashboardResponse getDashboard(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        return auditorDashboardService.getStats(from, to);
    }

    @GetMapping("/audit-log")
    public List<AuditLog> getAuditLog(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        return auditLogService.search(module, from, to);
    }
}
