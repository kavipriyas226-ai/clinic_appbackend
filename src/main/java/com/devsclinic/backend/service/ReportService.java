package com.devsclinic.backend.service;

import com.devsclinic.backend.dto.MedicineSalesResponse;
import com.devsclinic.backend.dto.PatientGrowthResponse;
import com.devsclinic.backend.dto.RevenueMonthResponse;
import com.devsclinic.backend.model.Invoice;
import com.devsclinic.backend.model.LineItem;
import com.devsclinic.backend.model.Patient;
import com.devsclinic.backend.repository.InvoiceRepository;
import com.devsclinic.backend.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Every report here is a live aggregation over real data (invoices = Payment History,
 * patients = the Patients module) — nothing is seeded or mocked.
 */
@Service
public class ReportService {

    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);

    private final InvoiceRepository invoiceRepository;
    private final PatientRepository patientRepository;

    public ReportService(InvoiceRepository invoiceRepository, PatientRepository patientRepository) {
        this.invoiceRepository = invoiceRepository;
        this.patientRepository = patientRepository;
    }

    private List<YearMonth> trailingSixMonths() {
        YearMonth current = YearMonth.from(LocalDate.now());
        return java.util.stream.IntStream.rangeClosed(0, 5)
                .mapToObj(i -> current.minusMonths(5 - i))
                .toList();
    }

    /** Revenue-by-month, summed from Paid invoices in the Payment History. */
    public List<RevenueMonthResponse> getRevenueByMonth() {
        List<Invoice> invoices = invoiceRepository.findAll();

        return trailingSixMonths().stream()
                .map(month -> {
                    double total = invoices.stream()
                            .filter(inv -> "Paid".equals(inv.getStatus()))
                            .filter(inv -> monthOf(inv.getDate()).map(m -> m.equals(month)).orElse(false))
                            .mapToDouble(Invoice::getTotal)
                            .sum();
                    return new RevenueMonthResponse(month.format(MONTH_LABEL), total);
                })
                .toList();
    }

    /**
     * New vs returning patients per month, derived from each patient's registration
     * date (Patient.lastVisit, set once at sign-up). "New" = registered that month;
     * "returning" = the existing patient base already registered before that month.
     */
    public List<PatientGrowthResponse> getPatientGrowth() {
        List<Patient> patients = patientRepository.findAll();

        return trailingSixMonths().stream()
                .map(month -> {
                    int newPatients = 0;
                    int returningPatients = 0;
                    for (Patient p : patients) {
                        var joined = monthOf(p.getLastVisit());
                        if (joined.isEmpty()) continue;
                        if (joined.get().equals(month)) {
                            newPatients++;
                        } else if (joined.get().isBefore(month)) {
                            returningPatients++;
                        }
                    }
                    return new PatientGrowthResponse(month.format(MONTH_LABEL), newPatients, returningPatients);
                })
                .toList();
    }

    /** Live aggregation over real invoices from the Payment History. */
    public List<MedicineSalesResponse> getMedicineSales() {
        Map<String, int[]> unitsByName = new LinkedHashMap<>(); // name -> [units]
        Map<String, Double> revenueByName = new LinkedHashMap<>();

        for (Invoice invoice : invoiceRepository.findAll()) {
            for (LineItem item : invoice.getLineItems()) {
                if (!"Medicine".equals(item.getType())) continue;
                unitsByName.merge(item.getName(), new int[]{item.getQty()}, (a, b) -> new int[]{a[0] + b[0]});
                revenueByName.merge(item.getName(), item.getAmount(), Double::sum);
            }
        }

        return unitsByName.keySet().stream()
                .map(name -> new MedicineSalesResponse(name, unitsByName.get(name)[0], revenueByName.get(name)))
                .sorted(Comparator.comparingDouble(MedicineSalesResponse::revenue).reversed())
                .limit(5)
                .toList();
    }

    private java.util.Optional<YearMonth> monthOf(String isoDate) {
        try {
            return java.util.Optional.of(YearMonth.from(LocalDate.parse(isoDate)));
        } catch (Exception e) {
            return java.util.Optional.empty();
        }
    }
}
