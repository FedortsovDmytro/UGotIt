package com.example.demo.base.service;

import com.example.demo.base.entity.Invoice;
import com.example.demo.base.entity.InvoiceStatus;
import com.example.demo.base.entity.Client;
import com.example.demo.base.repository.ClientRepository;
import com.example.demo.base.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    public InvoiceService(InvoiceRepository invoiceRepository, ClientRepository clientRepository) {
    this.invoiceRepository = invoiceRepository;
    this.clientRepository = clientRepository;
}
    public Invoice createInvoice(Long clientId, String invoiceNumber, LocalDate issueDate, LocalDate dueDate,
                                 BigDecimal amount, String currency, InvoiceStatus status) {

        if (dueDate.isBefore(issueDate))
            throw new IllegalArgumentException("Due date cannot be before issue date");

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalStateException("Client not found"));

        Invoice invoice = new Invoice.Builder(client, issueDate, dueDate, amount,status)
                .invoiceNumber(invoiceNumber)
                .currency(currency)
                .build();
        return invoiceRepository.save(invoice);
    }

    public List<Invoice> getInvoicesForClient(Long id) {
        Client client=clientRepository.findById(id).orElseThrow(() -> new IllegalStateException("Client not found"));
        return invoiceRepository.findByClient(client);
    }

    public List<Invoice> getOpenInvoices(Client client) {
        return invoiceRepository.findByClientAndStatus(client, InvoiceStatus.OPEN);
    }
    public Invoice getInvoice(Long invoiceId) {
    return invoiceRepository.findById(invoiceId).orElseThrow(() -> new IllegalStateException("Invoice not found"));
    }
}
