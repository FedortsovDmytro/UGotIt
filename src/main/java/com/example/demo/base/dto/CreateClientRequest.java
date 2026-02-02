package com.example.demo.dto;
import com.example.demo.entity.ClientStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateClientRequest(

        @NotBlank
        String externalId,

        @NotBlank
        String fullName,

        @NotNull
        ClientStatus status
) {}
