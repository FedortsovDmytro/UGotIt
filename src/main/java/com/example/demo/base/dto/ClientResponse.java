package com.example.demo.base.base.dto;

import com.example.demo.base.base.entity.Client;
import com.example.demo.base.base.entity.ClientStatus;

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