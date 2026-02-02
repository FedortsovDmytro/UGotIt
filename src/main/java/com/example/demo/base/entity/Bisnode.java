package com.example.demo.entity;

import jakarta.persistence.*;

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
    private String rating;     // ENX, KE, FEGA, brak

    protected Bisnode() {
    }

    public Bisnode(String clientName, Long externalId, Integer dax, String rating) {
        this.clientName = clientName;
        this.externalId = externalId;
        this.dax = dax;
        this.rating = rating;
    }


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
}
