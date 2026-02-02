package com.example.demo.domain;

import com.example.demo.base.entity.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PaymentTest {

    @Test
    void shouldBuildPaymentCorrectly() {
        Client client = new Client.Builder("ext1", "Client1", ClientStatus.ACTIVE).build();
        Invoice invoice = new Invoice.Builder(client,
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                BigDecimal.valueOf(500),
                InvoiceStatus.OPEN)
                .build();

        Payment payment = new Payment.Builder(invoice, LocalDate.now(), BigDecimal.valueOf(500))
                .method("BANK_TRANSFER")
                .build();

        assertEquals(invoice, payment.getInvoice());
        assertEquals(BigDecimal.valueOf(500), payment.getAmount());
        assertEquals("BANK_TRANSFER", payment.getMethod());
        assertNotNull(payment.getPaymentDate());
    }

    @Test
    void shouldThrowIfRequiredFieldsMissing() {
        Client client = new Client.Builder("ext1", "Client1", ClientStatus.ACTIVE).build();
        Invoice invoice = new Invoice.Builder(client,
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                BigDecimal.valueOf(500),
                InvoiceStatus.OPEN)
                .build();

        assertThrows(IllegalStateException.class,
                () -> new Payment.Builder(null, LocalDate.now(), BigDecimal.valueOf(100)).build());

        assertThrows(IllegalStateException.class,
                () -> new Payment.Builder(invoice, null, BigDecimal.valueOf(100)).build());

        assertThrows(IllegalStateException.class,
                () -> new Payment.Builder(invoice, LocalDate.now(), BigDecimal.valueOf(0)).build());
    }
}
