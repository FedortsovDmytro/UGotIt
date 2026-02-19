package com.example.demo.base.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
@Entity
@Table(name = "credit_limit")
public class CreditLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private Client client;

    @Column(nullable = false)
    private BigDecimal limitAmount;

    @Column(nullable = false)
    private BigDecimal usedAmount;

    @Column(nullable = false)
    private int paymentTermsDays;

    private LocalDate validFrom;
    private LocalDate validTo;

    protected CreditLimit() {}

    private CreditLimit(Builder b) {
        this.client = b.client;
        this.limitAmount = b.limitAmount;
        this.usedAmount = b.usedAmount;
        this.paymentTermsDays = b.paymentTermsDays;
        this.validFrom = b.validFrom;
        this.validTo = b.validTo;
    }


    public static class Builder {
        private final Client client;
        private final BigDecimal limitAmount;
        private final int paymentTermsDays;

        private BigDecimal usedAmount;
        private LocalDate validFrom;
        private LocalDate validTo;
        public Builder validFrom(LocalDate val) {
            this.validFrom = val;
            return this;
        }

        public Builder validTo(LocalDate val) {
            this.validTo = val;
            return this;
        }

        public CreditLimit build() {
            if (client == null) throw new IllegalStateException("client required");
            if (limitAmount == null || limitAmount.signum() <= 0)
                throw new IllegalStateException("limitAmount must be positive");
            if (paymentTermsDays <= 0)
                throw new IllegalStateException("paymentTermsDays must be positive");
            if (usedAmount == null)
                throw new IllegalStateException("usedAmount must be provided");

            return new CreditLimit(this);
        }

        public Builder(Client client, BigDecimal limitAmount, int paymentTermsDays) {
            this.client = client;
            this.limitAmount = limitAmount;
            this.paymentTermsDays = paymentTermsDays;
        }

        public Builder usedAmount(BigDecimal val) {
            this.usedAmount = val;
            return this;
        }

    }
    public boolean canCover(BigDecimal amount) {
        if (amount == null) return false;
        if (limitAmount == null || usedAmount == null) return false;

        return limitAmount.subtract(usedAmount).compareTo(amount) >= 0;
    }


    public Long getId() {
        return id;
    }

    public Client getClient() {
        return client;
    }

    public BigDecimal getLimitAmount() {
        return limitAmount;
    }

    public BigDecimal getUsedAmount() {
        return usedAmount;
    }

    public int getPaymentTermsDays() {
        return paymentTermsDays;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public void setUsedAmount(BigDecimal usedAmount) {
        this.usedAmount = usedAmount;
    }
    public void increaseUsedAmount(BigDecimal amount) {
        if (!canCover(amount)) {
            throw new IllegalStateException("Credit limit exceeded");
        }
        this.usedAmount = this.usedAmount.add(amount);
    }

    public void setPaymentTermsDays(int paymentTermsDays) {
        this.paymentTermsDays = paymentTermsDays;
    }
}
