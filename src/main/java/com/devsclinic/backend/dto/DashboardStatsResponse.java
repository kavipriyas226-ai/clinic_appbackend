package com.devsclinic.backend.dto;

public record DashboardStatsResponse(
        int totalPatients,
        int todaysPatients,
        double monthlyRevenue,
        int lowStockItems
) {
}
