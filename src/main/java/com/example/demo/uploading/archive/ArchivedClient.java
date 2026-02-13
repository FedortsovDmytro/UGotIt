package com.example.demo.uploading;


import com.example.demo.base.entity.Client;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "archived_clients")
public class ArchivedClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "credit_limit")
    private BigDecimal creditLimit;

    @Column(name = "rating_external")
    private String ratingExternal;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "archived_at", nullable = false)
    private LocalDateTime archivedAt;

    @Column(name = "archived_reason", nullable = false)
    private String archivedReason;

    protected ArchivedClient() {
        // JPA
    }


    public static ArchivedClient from(Client client, String reason) {
        ArchivedClient archived = new ArchivedClient();
        archived.externalId = client.getExternalId();
        archived.fullName = client.getName();
        archived.creditLimit = client.getCreditLimit();
        archived.ratingExternal = client.getRatingExternal();
        archived.createdAt = client.getCreatedAt();
        archived.archivedAt = LocalDateTime.now();
        archived.archivedReason = reason;
        return archived;
    }

    /* ---------- Getters ---------- */

    public Long getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getFullName() {
        return fullName;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public String getRatingExternal() {
        return ratingExternal;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getArchivedAt() {
        return archivedAt;
    }

    public String getArchivedReason() {
        return archivedReason;
    }
}
