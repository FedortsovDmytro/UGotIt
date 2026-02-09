package com.example.demo.base.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bisnode_rating")
public class Bisnode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @Column(name = "external_id")
    private Long externalId;

    @Column(name = "dax")
    private Integer dax;

    @Column(name = "rating")
    private String rating;
    protected Bisnode() {
    }
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id",nullable=false)
    private Client client;

    public Bisnode(String clientName, Long externalId, Integer dax, String rating) {
        this.clientName = clientName;
        this.externalId = externalId;
        this.dax = dax;
        this.rating = rating;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
    @Column(name = "fetched_at", nullable = true)
    private LocalDateTime fetchedAt;

    public Long getId() {
        return id;
    }

    public String getClientName() {
        return clientName;
    }

    public Long getExternalId() {
        return externalId;
    }

    public Integer getDax() {
        return dax;
    }

    public String getRating() {
        return rating;
    }

    public void setFetchedAt(LocalDateTime now) {
        this.fetchedAt = now;
    }
}
