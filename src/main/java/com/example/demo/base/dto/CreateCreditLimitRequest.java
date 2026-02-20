package com.example.demo.base.base.dto;

import java.math.BigDecimal;

public record CreateCreditLimitRequest(
        BigDecimal limitAmount,
        int paymentTermsDays
) {
}
