package com.example.demo.base.base.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
@Entity
@Table(name = "invoice")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Client client;

    private String invoiceNumber;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private BigDecimal amount;

    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;

    protected Invoice() {}

    private Invoice(Builder b) {
        this.client = b.client;
        this.invoiceNumber = b.invoiceNumber;
        this.issueDate = b.issueDate;
        this.dueDate = b.dueDate;
        this.amount = b.amount;
        this.currency = b.currency;
        this.status = b.status;
    }


    public static class Builder {
        private final Client client;
        private final LocalDate issueDate;
        private final LocalDate dueDate;
        private final BigDecimal amount;
        private final InvoiceStatus status;

        private String invoiceNumber;
        private String currency;

        public Builder(Client client, LocalDate issueDate, LocalDate dueDate,
                       BigDecimal amount, InvoiceStatus status) {
            this.client = client;
            this.issueDate = issueDate;
            this.dueDate = dueDate;
            this.amount = amount;
            this.status = status;
        }

        public Builder invoiceNumber(String val) {
            this.invoiceNumber = val;
            return this;
        }

        public Builder currency(String val) {
            this.currency = val;
            return this;
        }

        public Invoice build() {
            if (client == null) throw new IllegalStateException("client required");
            if (issueDate == null || dueDate == null)
                throw new IllegalStateException("dates required");
            if (amount == null || amount.signum() <= 0)
                throw new IllegalStateException("amount must be positive");
            if (status == null)
                throw new IllegalStateException("status required");

            return new Invoice(this);
        }
    }

    public Long getId() {
        return id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public Client getClient() {
        return client;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }
}
