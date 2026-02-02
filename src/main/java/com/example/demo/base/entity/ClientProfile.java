package com.example.demo.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "client_profile")
public class ClientProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private Client client;

    @Enumerated(EnumType.STRING)
    private BisnodeGroup bisnodeGroup;

    @Enumerated(EnumType.STRING)
    private DataSource dataSourceType;

    protected ClientProfile() {}

}
