package com.example.demo.base.uploading.dto;

import java.math.BigDecimal;

public class ReceivableRecord {

    private final String externalId;
    private final Integer daysPastDue;
    private final BigDecimal saldo;

    public ReceivableRecord(String externalId, Integer daysPastDue, BigDecimal saldo) {
        this.externalId = externalId;
        this.daysPastDue = daysPastDue;
        this.saldo = saldo;
    }

    public String getExternalId() {
        return externalId;
    }

    public Integer getDaysPastDue() {
        return daysPastDue;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }
}
