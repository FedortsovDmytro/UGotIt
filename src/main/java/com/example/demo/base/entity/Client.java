package com.example.demo.base.entity;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.ToStringSummary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @Column(name = "full_name")
    private String fullName;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClientStatus status;

    @Column(name = "credit_limit")
    private BigDecimal creditLimit;

    @Column(name = "rating_external")
    private String ratingExternal;
    @Column(name="created_at")
    private LocalDate createdAt;

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Invoice> invoices = new ArrayList<>();

    public Client() {}

    private Client(Builder builder) {
        this.externalId = builder.externalId;
        this.fullName = builder.name;
        this.status = builder.status;
        this.creditLimit = builder.creditLimit;
        this.ratingExternal = builder.ratingExternal;
        this.createdAt = builder.createdAt;

    }

    public static Builder builder(
            String externalId,
            String name,
            ClientStatus status
    ) {
        return new Builder(externalId, name, status);
    }

    public void setCreatedAt(LocalDate now) {
        this.createdAt = now;
    }

    public void setName(String clientName) {
        this.fullName = clientName;
    }

    public void setClientId(String clientId) {
        this.externalId = clientId;
    }


    public static class Builder {

        private final String externalId;
        private final String name;
        private final ClientStatus status;

        private final LocalDate createdAt=LocalDate.now();
        private BigDecimal creditLimit;
        private String ratingExternal;

        public Builder(String externalId, String name, ClientStatus status) {
            this.externalId = externalId;
            this.name = name;
            this.status = status;
        }

        public Builder creditLimit(BigDecimal creditLimit) {
            this.creditLimit = creditLimit;
            return this;
        }

        public Builder ratingExternal(String ratingExternal) {
            this.ratingExternal = ratingExternal;
            return this;
        }

        public Client build() {
            if (externalId == null || externalId.isBlank())
                throw new IllegalStateException("externalId required");
            if (name == null || name.isBlank())
                throw new IllegalStateException("name required");
            if (status == null)
                throw new IllegalStateException("status required");

            return new Client(this);
        }



    }

    public Long getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getName() {
        return fullName;
    }

    public ClientStatus getStatus() {
        return status;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public String getRatingExternal() {
        return ratingExternal;
    }

    public List<Invoice> getInvoices() {
        return invoices;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setStatus(ClientStatus status) {
        this.status = status;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public void setRatingExternal(String ratingExternal) {
        this.ratingExternal = ratingExternal;
    }

    public void setInvoices(List<Invoice> invoices) {
        this.invoices = invoices;
    }
}
