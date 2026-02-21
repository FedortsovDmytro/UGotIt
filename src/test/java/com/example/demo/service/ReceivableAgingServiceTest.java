package com.example.demo.service;
import com.example.demo.base.entity.Client;
import com.example.demo.base.entity.ClientStatus;
import com.example.demo.base.entity.Invoice;
import com.example.demo.base.entity.InvoiceStatus;
import com.example.demo.base.service.ReceivableAgingService;
import com.example.demo.risk.AgingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReceivableAgingServiceTest {

    private ReceivableAgingService agingService;
    private Client testClient;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        // Ініціалізація сервісу (без репозиторію, бо ми просто тестуємо calculate)
        agingService = new ReceivableAgingService(null);

        today = LocalDate.now();

        // Створення тестового клієнта
        testClient = Client.builder("EXT123", "Test Client", ClientStatus.ACTIVE).build();
    }

    @Test
    void testCalculateAgingBuckets() {
        // Створюємо інвойси з різним dueDate
        Invoice currentInvoice = new Invoice.Builder(testClient, today.minusDays(1), today.plusDays(2),
                BigDecimal.valueOf(100), InvoiceStatus.OPEN).build();

        Invoice overdue1to7 = new Invoice.Builder(testClient, today.minusDays(5), today.minusDays(3),
                BigDecimal.valueOf(200), InvoiceStatus.OPEN).build();

        Invoice overdue8to14 = new Invoice.Builder(testClient, today.minusDays(12), today.minusDays(10),
                BigDecimal.valueOf(300), InvoiceStatus.OPEN).build();

        Invoice overdue15to30 = new Invoice.Builder(testClient, today.minusDays(20), today.minusDays(18),
                BigDecimal.valueOf(400), InvoiceStatus.OPEN).build();

        Invoice overdue31to60 = new Invoice.Builder(testClient, today.minusDays(50), today.minusDays(40),
                BigDecimal.valueOf(500), InvoiceStatus.OPEN).build();

        Invoice overdue60Plus = new Invoice.Builder(testClient, today.minusDays(120), today.minusDays(100),
                BigDecimal.valueOf(600), InvoiceStatus.OPEN).build();

        List<Invoice> invoices = List.of(currentInvoice, overdue1to7, overdue8to14,
                overdue15to30, overdue31to60, overdue60Plus);

        AgingResult result = agingService.calculate(invoices);

    }
}