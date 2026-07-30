package com.devsclinic.backend.controller;

import com.devsclinic.backend.dto.MedicineSalesResponse;
import com.devsclinic.backend.dto.PatientGrowthResponse;
import com.devsclinic.backend.dto.RevenueMonthResponse;
import com.devsclinic.backend.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/revenue")
    public List<RevenueMonthResponse> getRevenue() {
        return reportService.getRevenueByMonth();
    }

    @GetMapping("/patient-growth")
    public List<PatientGrowthResponse> getPatientGrowth() {
        return reportService.getPatientGrowth();
    }

    @GetMapping("/medicine-sales")
    public List<MedicineSalesResponse> getMedicineSales() {
        return reportService.getMedicineSales();
    }
}
