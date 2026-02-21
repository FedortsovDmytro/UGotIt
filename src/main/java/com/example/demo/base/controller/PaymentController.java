package com.example.demo.base.controller;

import com.example.demo.base.dto.CreatePaymentRequest;
import com.example.demo.base.entity.Payment;
import com.example.demo.base.service.ClientService;
import com.example.demo.base.service.InvoiceService;
import com.example.demo.base.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/clients/{externalId}/invoices/{invoiceId}/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final InvoiceService invoiceService;
    private final ClientService clientService;

    public PaymentController(
            PaymentService paymentService,
            InvoiceService invoiceService,
            ClientService clientService
    ) {
        this.paymentService = paymentService;
        this.invoiceService = invoiceService;
        this.clientService = clientService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Payment createPayment(
            @PathVariable String externalId,
            @PathVariable Long invoiceId,
            @RequestBody CreatePaymentRequest request
    ) {
        clientService.getByExternalId(externalId);

        return paymentService.createPayment(
                invoiceService.getInvoice(invoiceId),
                request.amount(),
                request.paymentDate(),
                request.method()
        );
    }

    @GetMapping
    public List<Payment> getPaymentsForInvoice(
            @PathVariable String externalId,
            @PathVariable Long invoiceId
    ) {
        clientService.getByExternalId(externalId);
        return paymentService.getPaymentsForInvoice(
                invoiceService.getInvoice(invoiceId)
        );
    }
}
