package com.example.demo.base.base.dto;
import com.example.demo.base.base.entity.ClientStatus;
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
