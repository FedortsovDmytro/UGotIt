package com.example.demo.controller;


import com.example.demo.entity.Client;
import com.example.demo.entity.Invoice;
import com.example.demo.service.ClientService;
import com.example.demo.service.InvoiceService;
import com.example.demo.dto.CreateInvoiceRequest;
import com.example.demo.dto.InvoiceResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/clients/{externalId}/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final ClientService clientService;

    public InvoiceController(InvoiceService invoiceService,
                             ClientService clientService) {
        this.invoiceService = invoiceService;
        this.clientService = clientService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Invoice createInvoice(
            @PathVariable String externalId,
            @RequestBody CreateInvoiceRequest request
    ) {
        Client client = clientService.getByExternalId(externalId);

        return invoiceService.createInvoice(
                client.getId(),
                request.invoiceNumber(),
                request.issueDate(),
                request.dueDate(),
                request.amount(),
                request.currency(),
                request.status()
        );
    }

    @GetMapping
    public List<Invoice> getInvoices(@PathVariable String externalId) {
        Client client = clientService.getByExternalId(externalId);
        return invoiceService.getInvoicesForClient(client.getId());
    }
}
