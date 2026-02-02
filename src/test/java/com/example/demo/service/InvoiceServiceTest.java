package com.example.demo.service;

import com.example.demo.base.entity.Client;
import com.example.demo.base.entity.ClientStatus;
import com.example.demo.base.entity.Invoice;
import com.example.demo.base.entity.InvoiceStatus;
import com.example.demo.base.repository.ClientRepository;
import com.example.demo.base.repository.InvoiceRepository;
import com.example.demo.base.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;
class InvoiceServiceTest {

    private InvoiceRepository invoiceRepository;
    private ClientRepository clientRepository;
    private InvoiceService invoiceService;

    private Client client;

    @BeforeEach
    void setUp() {
        invoiceRepository = mock(InvoiceRepository.class);
        clientRepository = mock(ClientRepository.class);
        invoiceService = new InvoiceService(invoiceRepository, clientRepository);

        client = Client.builder("ext-1", "Alice", ClientStatus.ACTIVE).build();
    }

    @Test
    void createInvoice_shouldSaveInvoice() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Invoice result = invoiceService.createInvoice(
                1L,
                "INV-001",
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                BigDecimal.valueOf(1000),
                "USD",
                InvoiceStatus.OPEN
        );

        assertThat(result.getInvoiceNumber()).isEqualTo("INV-001");
        assertThat(result.getClient()).isEqualTo(client);

        verify(clientRepository).findById(1L);
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void createInvoice_shouldFail_whenClientNotFound() {
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                invoiceService.createInvoice(
                        1L,
                        "INV-001",
                        LocalDate.now(),
                        LocalDate.now().plusDays(10),
                        BigDecimal.TEN,
                        "USD",
                        InvoiceStatus.OPEN
                )
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Client not found");
    }

    @Test
    void createInvoice_shouldFail_whenDueDateBeforeIssueDate() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        assertThatThrownBy(() ->
                invoiceService.createInvoice(
                        1L,
                        "INV-001",
                        LocalDate.now(),
                        LocalDate.now().minusDays(1),
                        BigDecimal.TEN,
                        "USD",
                        InvoiceStatus.OPEN
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getInvoicesForClient_shouldReturnInvoices() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(invoiceRepository.findByClient(client))
                .thenReturn(List.of(mock(Invoice.class)));

        List<Invoice> result = invoiceService.getInvoicesForClient(1L);

        assertThat(result).hasSize(1);
        verify(invoiceRepository).findByClient(client);
    }
}
