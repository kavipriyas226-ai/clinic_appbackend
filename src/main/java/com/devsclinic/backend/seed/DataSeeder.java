package com.devsclinic.backend.seed;

import com.devsclinic.backend.model.*;
import com.devsclinic.backend.repository.*;
import com.devsclinic.backend.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Seeds MongoDB with the app's original mock dataset the first time each collection
 * is empty, so the app has real, meaningful data out of the box. Never overwrites
 * existing data on subsequent restarts.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AccountRepository accountRepository;
    private final ClinicProfileRepository clinicProfileRepository;
    private final TreatmentOptionRepository treatmentOptionRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public DataSeeder(
            AccountRepository accountRepository,
            ClinicProfileRepository clinicProfileRepository,
            TreatmentOptionRepository treatmentOptionRepository,
            InventoryItemRepository inventoryItemRepository,
            PatientRepository patientRepository,
            PasswordEncoder passwordEncoder,
            NotificationService notificationService
    ) {
        this.accountRepository = accountRepository;
        this.clinicProfileRepository = clinicProfileRepository;
        this.treatmentOptionRepository = treatmentOptionRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    @Override
    public void run(String... args) {
        seedAccount();
        seedClinicProfile();
        seedTreatmentOptions();
        seedInventory();
        seedPatients();
        backfillLowStockNotifications();
    }

    /**
     * One-time (per item) catch-up: raises a low-stock notification for any item that is
     * already at/under threshold but hasn't been flagged yet — covers items that were low
     * before this notification feature existed. Safe to run on every startup: the
     * lowStockNotified flag makes it a no-op once an item has already been notified.
     */
    private void backfillLowStockNotifications() {
        List<InventoryItem> toNotify = inventoryItemRepository.findAll().stream()
                .filter(item -> item.getStock() <= item.getThreshold() && !item.isLowStockNotified())
                .toList();
        if (toNotify.isEmpty()) return;

        toNotify.forEach(item -> {
            notificationService.createLowStockNotification(item);
            item.setLowStockNotified(true);
        });
        inventoryItemRepository.saveAll(toNotify);
        log.info("Backfilled {} low-stock notification(s)", toNotify.size());
    }

    private void seedAccount() {
        if (accountRepository.count() > 0) return;
        accountRepository.save(Account.builder()
                .username("devs.hairandskinclinic2026@gmail.com")
                .passwordHash(passwordEncoder.encode("password"))
                .build());
        log.info("Seeded account");
    }

    private void seedClinicProfile() {
        if (clinicProfileRepository.count() > 0) return;
        clinicProfileRepository.save(ClinicProfile.builder()
                .name("Devs Hair & Skin Clinic")
                .tagline("Advanced Dermatology & Trichology Care")
                .address("14 Lotus Avenue, Bandra West, Mumbai, MH 400050")
                .phone("+91 98200 12345")
                .email("hello@devshairandskinclinic.in")
                .gstin("27AACCD1234F1Z5")
                .build());
        log.info("Seeded clinic profile");
    }

    // Imported from "clinic treatments.xlsx" (Skin Treatments / Hair Treatments /
    // Cosmetic Procedures). The spreadsheet only listed treatment names — prices below
    // are reasonable estimates (per the user's choice) and should be reviewed/adjusted
    // in the Inventory module.
    private void seedTreatmentOptions() {
        if (treatmentOptionRepository.count() > 0) return;
        treatmentOptionRepository.saveAll(List.of(
                treatment("TR-01", "Acne & Acne Scar Treatment", "Skin Treatments", 3500),
                treatment("TR-02", "Pigmentation Removal", "Skin Treatments", 3000),
                treatment("TR-03", "Skin Brightening", "Skin Treatments", 2500),
                treatment("TR-04", "Chemical Peel", "Skin Treatments", 2500),
                treatment("TR-05", "Hydra Facial", "Skin Treatments", 2800),
                treatment("TR-06", "Laser Skin Rejuvenation", "Skin Treatments", 5500),
                treatment("TR-07", "Anti-Aging Treatments", "Skin Treatments", 4500),
                treatment("TR-08", "Skin Tightening", "Skin Treatments", 6000),
                treatment("TR-09", "Mole & Wart Removal", "Skin Treatments", 1800),
                treatment("TR-10", "Dark Circle Treatment", "Skin Treatments", 2200),
                treatment("TR-11", "Hair Fall Treatment", "Hair Treatments", 3000),
                treatment("TR-12", "Hair Regrowth Therapy", "Hair Treatments", 4000),
                treatment("TR-13", "PRP Hair Treatment", "Hair Treatments", 6000),
                treatment("TR-14", "Hair Transplant Consultation", "Hair Treatments", 800),
                treatment("TR-15", "Dandruff Treatment", "Hair Treatments", 1500),
                treatment("TR-16", "Scalp Care Therapy", "Hair Treatments", 2000),
                treatment("TR-17", "Hair Strengthening", "Hair Treatments", 2500),
                treatment("TR-18", "Hair Nourishment Programs", "Hair Treatments", 3000),
                treatment("TR-19", "Botox Consultation", "Cosmetic Procedures", 800),
                treatment("TR-20", "Dermal Fillers", "Cosmetic Procedures", 12000),
                treatment("TR-21", "Laser Hair Reduction", "Cosmetic Procedures", 4500),
                treatment("TR-22", "Skin Polishing", "Cosmetic Procedures", 2200),
                treatment("TR-23", "Carbon Laser Facial", "Cosmetic Procedures", 3500),
                treatment("TR-24", "Microneedling", "Cosmetic Procedures", 3200),
                treatment("TR-25", "Stretch Mark Reduction", "Cosmetic Procedures", 4000),
                treatment("TR-26", "Tattoo Removal", "Cosmetic Procedures", 5000)
        ));
        log.info("Seeded treatment options");
    }

    private TreatmentOption treatment(String id, String name, String category, double price) {
        return TreatmentOption.builder().id(id).name(name).category(category).price(price).build();
    }

    private List<InventoryItem> seedInventory() {
        if (inventoryItemRepository.count() > 0) return inventoryItemRepository.findAll();

        List<InventoryItem> items = List.of(
                item("MED-101", "Melan Tran3x gel", "Topical", 6900, 42, 15, "2026-08-15", "One Clinical Skincare Distributors"),
                item("MED-102", "trichology shampoo", "Hair Care", 4700, 4, 20, "2026-09-11", "One Clinical Skincare Distributors"),
                item("MED-103", "brightening foam", "Skin Care", 3500, 58, 15, "2026-10-08", "One Clinical Skincare Distributors"),
                item("MED-104", "Meso Sunscreen", "Skin Care", 5000, 6, 15, "2026-11-04", "Meso Aesthetics Supply Co."),
                item("MED-105", "trichology hair loss lotion", "Hair Care", 6600, 31, 20, "2026-12-01", "One Clinical Skincare Distributors"),
                item("MED-106", "melan recovery", "Skin Care", 6900, 76, 15, "2026-12-28", "One Clinical Skincare Distributors"),
                item("MED-107", "blemiderm treatment 50Ml", "Skin Care", 6500, 12, 15, "2027-01-24", "One Clinical Skincare Distributors"),
                item("MED-108", "purifying mousse 150Ml", "Skin Care", 4500, 9, 15, "2027-02-20", "One Clinical Skincare Distributors"),
                item("MED-109", "Hydra vital mask 100Ml", "Skin Care", 5800, 25, 15, "2027-03-19", "One Clinical Skincare Distributors"),
                item("MED-110", "Hydra vital light 50Ml", "Skin Care", 5900, 50, 15, "2027-04-15", "One Clinical Skincare Distributors"),
                item("MED-111", "mesolips protector 15Ml", "Skin Care", 1800, 18, 15, "2027-05-12", "Meso Aesthetics Supply Co."),
                item("MED-112", "age element brightening conc 30Ml", "Skin Care", 10500, 3, 15, "2027-06-08", "One Clinical Skincare Distributors"),
                item("MED-113", "age element brightening cream 50Ml", "Skin Care", 8000, 33, 15, "2027-07-05", "One Clinical Skincare Distributors"),
                item("MED-114", "age element brightening complex kit", "Skin Care", 8000, 60, 15, "2027-08-01", "One Clinical Skincare Distributors"),
                item("MED-115", "age element brightening eye contour 15Ml", "Skin Care", 6600, 15, 15, "2027-08-28", "One Clinical Skincare Distributors"),
                item("MED-116", "hydra vital mask 500Ml", "Skin Care", 22000, 8, 15, "2027-09-24", "One Clinical Skincare Distributors"),
                item("MED-117", "one moist lotion", "Skin Care", 250, 45, 15, "2027-10-21", "Devs Skin & Hair Essentials Pvt Ltd"),
                item("MED-118", "eerace - M hydra gel", "Topical", 690, 20, 15, "2027-11-17", "One Clinical Skincare Distributors"),
                item("MED-119", "OG - Sunscreen 50+", "Skin Care", 699, 55, 15, "2027-12-14", "One Clinical Skincare Distributors"),
                item("MED-120", "one acne soap", "Skin Care", 140, 10, 15, "2028-01-10", "Devs Skin & Hair Essentials Pvt Ltd"),
                item("MED-121", "one moist soap", "Skin Care", 120, 38, 15, "2028-02-06", "Devs Skin & Hair Essentials Pvt Ltd"),
                item("MED-122", "one glam soap", "Skin Care", 170, 22, 15, "2028-03-04", "Devs Skin & Hair Essentials Pvt Ltd"),
                item("MED-123", "klyre brightening serum", "Skin Care", 1457, 5, 15, "2028-03-31", "One Clinical Skincare Distributors"),
                item("MED-124", "D. Dan serum", "Skin Care", 1359, 48, 15, "2028-04-27", "One Clinical Skincare Distributors"),
                item("MED-125", "Devs Sali face wash", "Skin Care", 500, 16, 15, "2028-05-24", "Devs Skin & Hair Essentials Pvt Ltd"),
                item("MED-126", "Olnatrue gluta face wash", "Skin Care", 250, 29, 15, "2028-06-20", "One Clinical Skincare Distributors"),
                item("MED-127", "Devs pink clay face wash", "Skin Care", 500, 62, 15, "2028-07-17", "Devs Skin & Hair Essentials Pvt Ltd"),
                item("MED-128", "Devs intensive hair growth serum", "Hair Care", 3500, 7, 20, "2028-08-13", "Devs Skin & Hair Essentials Pvt Ltd"),
                item("MED-129", "Devs anti dandruff shampoo", "Hair Care", 500, 40, 20, "2028-09-09", "Devs Skin & Hair Essentials Pvt Ltd")
        );
        inventoryItemRepository.saveAll(items);
        log.info("Seeded {} inventory items", items.size());
        return items;
    }

    private InventoryItem item(String id, String name, String category, double price, int stock, int threshold, String expiry, String supplier) {
        return InventoryItem.builder().id(id).name(name).category(category).price(price)
                .stock(stock).threshold(threshold).expiry(expiry).supplier(supplier).build();
    }

    private List<Patient> seedPatients() {
        if (patientRepository.count() > 0) return patientRepository.findAll();

        List<Patient> patients = new ArrayList<>(List.of(
                patient("PT-1001", "Riya Sharma", 28, "Female", "+91 90000 11122", "Acne & Scarring", "2026-07-20", "New", "Dr. Anita Rao"),
                patient("PT-1002", "Karan Mehta", 34, "Male", "+91 90000 22233", "Hair Fall", "2026-07-18", "New", "Dr. Vikram Sen"),
                patient("PT-1003", "Ananya Iyer", 22, "Female", "+91 90000 33344", "Pigmentation", "2026-07-15", "New", "Dr. Anita Rao"),
                patient("PT-1004", "Rohit Verma", 45, "Male", "+91 90000 44455", "Psoriasis", "2026-07-12", "New", "Dr. Neha Kapoor"),
                patient("PT-1005", "Simran Kaur", 30, "Female", "+91 90000 55566", "Hair Transplant Consult", "2026-07-10", "New", "Dr. Vikram Sen"),
                patient("PT-1006", "Aditya Nair", 26, "Male", "+91 90000 66677", "Eczema", "2026-07-08", "New", "Dr. Anita Rao"),
                patient("PT-1007", "Meera Joshi", 39, "Female", "+91 90000 77788", "Anti-Aging", "2026-07-05", "New", "Dr. Neha Kapoor"),
                patient("PT-1008", "Farhan Ali", 31, "Male", "+91 90000 88899", "Dandruff & Scalp Care", "2026-07-02", "New", "Dr. Vikram Sen"),
                patient("PT-1009", "Sneha Reddy", 24, "Female", "+91 90000 99900", "Acne", "2026-06-29", "New", "Dr. Anita Rao"),
                patient("PT-1010", "Vikas Gupta", 50, "Male", "+91 90000 10101", "Hair Fall", "2026-06-25", "Inactive", "Dr. Neha Kapoor"),
                patient("PT-1011", "Priya Desai", 27, "Female", "+91 90000 20202", "Melasma", "2026-06-20", "New", "Dr. Anita Rao"),
                patient("PT-1012", "Arjun Rathore", 33, "Male", "+91 90000 30303", "Beard Restoration", "2026-06-18", "Inactive", "Dr. Vikram Sen")
        ));

        Patient riya = patients.get(0);
        riya.setDob("1998-03-14");
        riya.setEmail("riya.sharma@example.com");
        riya.setAddress("A-204, Silver Oak, Andheri East, Mumbai");
        riya.setBloodGroup("O+");
        riya.setAllergies("None known");
        riya.setMedicalNotes("Sensitive skin, prone to hyperpigmentation post-acne. Avoid retinoids above 0.1%.");
        riya.setTreatments(new ArrayList<>(List.of(
                PatientTreatment.builder().date("2026-07-20").name("Chemical Peel (Salicylic 30%)").doctor("Dr. Anita Rao").notes("Tolerated well, mild redness resolved in 24h.").build(),
                PatientTreatment.builder().date("2026-06-10").name("Consultation + Skin Analysis").doctor("Dr. Anita Rao").notes("Started on topical retinoid + niacinamide serum.").build()
        )));
        riya.setPrescriptions(new ArrayList<>(List.of(
                PatientPrescriptionEntry.builder().date("2026-07-20").medicine("Melan Tran3x gel").dosage("Apply at night, pea-sized amount").build(),
                PatientPrescriptionEntry.builder().date("2026-07-20").medicine("OG - Sunscreen 50+").dosage("Apply every morning").build()
        )));

        patientRepository.saveAll(patients);
        log.info("Seeded {} patients", patients.size());
        return patients;
    }

    private Patient patient(String id, String name, int age, String gender, String phone, String concern, String lastVisit, String status, String doctor) {
        return Patient.builder()
                .id(id).name(name).age(age).gender(gender).phone(phone).concern(concern)
                .lastVisit(lastVisit).status(status).doctor(doctor)
                .treatments(new ArrayList<>()).prescriptions(new ArrayList<>()).invoices(new ArrayList<>())
                .build();
    }
}
