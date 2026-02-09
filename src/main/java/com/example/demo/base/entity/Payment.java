package com.example.demo.base.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Invoice invoice;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Column(nullable = false)
    private BigDecimal amount;

    private String method;

    protected Payment() {}

    private Payment(Builder b) {
        this.invoice = b.invoice;
        this.paymentDate = b.paymentDate;
        this.amount = b.amount;
        this.method = b.method;
    }

    public Long getId() {
        return id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getMethod() {
        return method;
    }

    public static class Builder {
        private final Invoice invoice;
        private final LocalDate paymentDate;
        private final BigDecimal amount;

        private String method;

        public Builder(Invoice invoice, LocalDate paymentDate, BigDecimal amount) {
            this.invoice = invoice;
            this.paymentDate = paymentDate;
            this.amount = amount;
        }

        public Builder method(String val) {
            this.method = val;
            return this;
        }

        public Payment build() {
            if (invoice == null) throw new IllegalStateException("invoice required");
            if (paymentDate == null)
                throw new IllegalStateException("paymentDate required");
            if (amount == null || amount.signum() <= 0)
                throw new IllegalStateException("amount must be positive");

            return new Payment(this);
        }
    }
}
