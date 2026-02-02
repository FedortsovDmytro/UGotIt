package com.example.demo.dto;

import com.example.demo.entity.Client;
import com.example.demo.entity.ClientStatus;

public record ClientResponse(
        String externalId,
        String name,
        ClientStatus status
) {
    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getExternalId(),
                client.getName(),
                client.getStatus()
        );
    }
}