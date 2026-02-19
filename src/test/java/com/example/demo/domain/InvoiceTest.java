//package com.example.demo.domain;
//
//import com.example.demo.base.entity.Client;
//import com.example.demo.base.entity.ClientStatus;
//import com.example.demo.base.entity.Invoice;
//import com.example.demo.base.entity.InvoiceStatus;
//import org.junit.jupiter.api.Test;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class InvoiceTest {
//
//    @Test
//    void shouldBuildInvoiceCorrectly() {
//        Client client = new Client.Builder("ext1", "Client1", ClientStatus.ACTIVE).build();
//
//        Invoice invoice = new Invoice.Builder(client,
//                LocalDate.now(),
//                LocalDate.now().plusDays(30),
//                BigDecimal.valueOf(500),
//                InvoiceStatus.OPEN)
//                .invoiceNumber("INV-001")
//                .currency("PLN")
//                .build();
//
//        assertEquals(client, invoice.getClient());
//        assertEquals(BigDecimal.valueOf(500), invoice.getAmount());
//        assertEquals("INV-001", invoice.getInvoiceNumber());
//        assertEquals("PLN", invoice.getCurrency());
//        assertEquals(InvoiceStatus.OPEN, invoice.getStatus());
//    }
//
//    @Test
//    void shouldThrowIfRequiredFieldsMissing() {
//        Client client = new Client.Builder("ext1", "Client1", ClientStatus.ACTIVE).build();
//
//        assertThrows(IllegalStateException.class,
//                () -> new Invoice.Builder(null, LocalDate.now(),
//                        LocalDate.now().plusDays(30), BigDecimal.valueOf(100), InvoiceStatus.OPEN)
//                        .build());
//
//        assertThrows(IllegalStateException.class,
//                () -> new Invoice.Builder(client, null,
//                        LocalDate.now().plusDays(30), BigDecimal.valueOf(100), InvoiceStatus.OPEN)
//                        .build());
//
//        assertThrows(IllegalStateException.class,
//                () -> new Invoice.Builder(client, LocalDate.now(),
//                        null, BigDecimal.valueOf(100), InvoiceStatus.OPEN)
//                        .build());
//
//        assertThrows(IllegalStateException.class,
//                () -> new Invoice.Builder(client, LocalDate.now(),
//                        LocalDate.now().plusDays(30), null, InvoiceStatus.OPEN)
//                        .build());
//
//        assertThrows(IllegalStateException.class,
//                () -> new Invoice.Builder(client, LocalDate.now(),
//                        LocalDate.now().plusDays(30), BigDecimal.valueOf(100), null)
//                        .build());
//    }
//}
