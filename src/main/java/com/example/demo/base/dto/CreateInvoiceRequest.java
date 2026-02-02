package com.example.demo.base.dto;


import com.example.demo.base.entity.InvoiceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateInvoiceRequest(

        @NotNull
        Long clientId,

        String invoiceNumber,

        @NotNull
        LocalDate issueDate,

        @NotNull
        LocalDate dueDate,

        @NotNull
        @Positive
        BigDecimal amount,

        String currency,

        @NotNull
        InvoiceStatus status
) {}
