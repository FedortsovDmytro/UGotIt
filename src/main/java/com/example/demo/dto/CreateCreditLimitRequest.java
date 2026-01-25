package com.example.demo.dto;

import java.math.BigDecimal;

public record CreateCreditLimitRequest(
        BigDecimal limitAmount,
        int paymentTermsDays
) {
}
