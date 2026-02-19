package com.example.demo.uploading.archive;


import com.example.demo.base.entity.Invoice;
import com.example.demo.base.entity.InvoiceStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "archived_invoice")
public class ArchivedInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false)
    private Long originalInvoiceId;

    @Column(nullable = false)
    private String clientExternalId;

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

    @Column(nullable = false)
    private LocalDate archivedAt;

    protected ArchivedInvoice() {}

    private ArchivedInvoice(Builder b) {
        this.originalInvoiceId = b.originalInvoiceId;
        this.clientExternalId = b.clientExternalId;
        this.invoiceNumber = b.invoiceNumber;
        this.issueDate = b.issueDate;
        this.dueDate = b.dueDate;
        this.amount = b.amount;
        this.currency = b.currency;
        this.status = b.status;
        this.archivedAt = b.archivedAt;
    }

    public static ArchivedInvoice fromInvoice(Invoice invoice) {
        return new Builder(
                invoice.getId(),
                invoice.getClient().getExternalId(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getAmount(),
                invoice.getStatus()
        )
                .invoiceNumber(invoice.getInvoiceNumber())
                .currency(invoice.getCurrency())
                .build();
    }

    public static class Builder {

        private final Long originalInvoiceId;
        private final String clientExternalId;
        private final LocalDate issueDate;
        private final LocalDate dueDate;
        private final BigDecimal amount;
        private final InvoiceStatus status;

        private String invoiceNumber;
        private String currency;
        private final LocalDate archivedAt = LocalDate.now();

        public Builder(
                Long originalInvoiceId,
                String clientExternalId,
                LocalDate issueDate,
                LocalDate dueDate,
                BigDecimal amount,
                InvoiceStatus status
        ) {
            this.originalInvoiceId = originalInvoiceId;
            this.clientExternalId = clientExternalId;
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

        public ArchivedInvoice build() {
            if (originalInvoiceId == null)
                throw new IllegalStateException("originalInvoiceId required");
            if (clientExternalId == null || clientExternalId.isBlank())
                throw new IllegalStateException("clientExternalId required");
            if (issueDate == null || dueDate == null)
                throw new IllegalStateException("dates required");
            if (amount == null || amount.signum() <= 0)
                throw new IllegalStateException("amount must be positive");
            if (status == null)
                throw new IllegalStateException("status required");

            return new ArchivedInvoice(this);
        }
    }

    public Long getId() {
        return id;
    }

    public Long getOriginalInvoiceId() {
        return originalInvoiceId;
    }

    public String getClientExternalId() {
        return clientExternalId;
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

    public LocalDate getArchivedAt() {
        return archivedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOriginalInvoiceId(Long originalInvoiceId) {
        this.originalInvoiceId = originalInvoiceId;
    }

    public void setClientExternalId(String clientExternalId) {
        this.clientExternalId = clientExternalId;
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

    public void setArchivedAt(LocalDate archivedAt) {
        this.archivedAt = archivedAt;
    }
}

