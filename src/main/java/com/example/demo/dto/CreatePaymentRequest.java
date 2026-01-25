package com.example.demo.dto;


import java.math.BigDecimal;
import java.time.LocalDate;

public record CreatePaymentRequest(
        Long invoiceId,
        BigDecimal amount,
        LocalDate paymentDate,
        String method
) {}