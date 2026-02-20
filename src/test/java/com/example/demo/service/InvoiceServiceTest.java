package com.example.demo.base.service;


import com.example.demo.base.base.entity.Client;
import com.example.demo.base.base.entity.Invoice;
import com.example.demo.base.base.entity.InvoiceStatus;
import com.example.demo.base.base.repository.ClientRepository;
import com.example.demo.base.base.repository.InvoiceRepository;
import com.example.demo.base.base.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ClientRepository clientRepository;

    private InvoiceService invoiceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        invoiceService = new InvoiceService(invoiceRepository, clientRepository);
    }

    @Test
    void shouldCreateInvoiceSuccessfully() {
        Client client = new Client.Builder("ext123", "John Doe", null).build();
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        Invoice invoice = new Invoice.Builder(client, LocalDate.now(), LocalDate.now().plusDays(10),
                BigDecimal.valueOf(1000), InvoiceStatus.OPEN)
                .invoiceNumber("INV-001")
                .currency("USD")
                .build();

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        Invoice result = invoiceService.createInvoice(1L, "INV-001", LocalDate.now(),
                LocalDate.now().plusDays(10), BigDecimal.valueOf(1000), "USD", InvoiceStatus.OPEN);

        assertThat(result).isNotNull();
        assertThat(result.getClient()).isEqualTo(client);
        assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000));

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(captor.capture());
        assertThat(captor.getValue().getInvoiceNumber()).isEqualTo("INV-001");
    }

    @Test
    void shouldThrowIfDueDateBeforeIssueDate() {
        assertThrows(IllegalArgumentException.class,
                () -> invoiceService.createInvoice(1L, "INV-002", LocalDate.now(),
                        LocalDate.now().minusDays(1), BigDecimal.valueOf(500), "USD", InvoiceStatus.OPEN));
    }

    @Test
    void shouldThrowIfClientNotFound() {
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> invoiceService.createInvoice(999L, "INV-003", LocalDate.now(),
                        LocalDate.now().plusDays(5), BigDecimal.valueOf(500), "USD", InvoiceStatus.OPEN));
    }

    @Test
    void shouldGetInvoicesForClient() {
        Client client = new Client.Builder("ext123", "John Doe", null).build();
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        Invoice invoice1 = new Invoice.Builder(client, LocalDate.now(), LocalDate.now().plusDays(10),
                BigDecimal.valueOf(1000), InvoiceStatus.OPEN).build();
        Invoice invoice2 = new Invoice.Builder(client, LocalDate.now(), LocalDate.now().plusDays(5),
                BigDecimal.valueOf(500), InvoiceStatus.PAID).build();

        when(invoiceRepository.findByClient(client)).thenReturn(List.of(invoice1, invoice2));

        List<Invoice> result = invoiceService.getInvoicesForClient(1L);
        assertThat(result).hasSize(2);
        assertThat(result).contains(invoice1, invoice2);
    }

    @Test
    void shouldGetOpenInvoices() {
        Client client = new Client.Builder("ext123", "John Doe", null).build();

        Invoice invoice1 = new Invoice.Builder(client, LocalDate.now(), LocalDate.now().plusDays(10),
                BigDecimal.valueOf(1000), InvoiceStatus.OPEN).build();
        Invoice invoice2 = new Invoice.Builder(client, LocalDate.now(), LocalDate.now().plusDays(5),
                BigDecimal.valueOf(500), InvoiceStatus.PAID).build();

        when(invoiceRepository.findByClientAndStatus(client, InvoiceStatus.OPEN))
                .thenReturn(List.of(invoice1));

        List<Invoice> result = invoiceService.getOpenInvoices(client);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(InvoiceStatus.OPEN);
    }

    @Test
    void shouldGetInvoiceById() {
        Client client = new Client.Builder("ext123", "John Doe", null).build();
        Invoice invoice = new Invoice.Builder(client, LocalDate.now(), LocalDate.now().plusDays(10),
                BigDecimal.valueOf(1000), InvoiceStatus.OPEN).build();

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        Invoice result = invoiceService.getInvoice(1L);
        assertThat(result).isEqualTo(invoice);
    }

    @Test
    void shouldThrowIfInvoiceNotFound() {
        when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> invoiceService.getInvoice(999L));
    }
}