package com.example.demo.base.dto;

import com.example.demo.base.entity.Invoice;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceResponse(
        Long id,
        String invoiceNumber,
        LocalDate issueDate,
        LocalDate dueDate,
        BigDecimal amount,
        String currency,
        String status,
        Long clientId
) {
    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getAmount(),
                invoice.getCurrency(),
                invoice.getStatus().name(),
                invoice.getClient().getId()
        );
    }
}