package com.example.demo.base.base.controller;

import com.example.demo.base.base.dto.ClientResponse;
import com.example.demo.base.base.dto.CreateClientRequest;
import com.example.demo.base.base.entity.Client;
import com.example.demo.base.base.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ClientResponse> createClient(@RequestBody @Valid CreateClientRequest request) {
        Client client = clientService.createClient(
                request.externalId(),
                request.fullName(),
                request.status()
        );
        ClientResponse response = ClientResponse.from(client);

        URI location = URI.create("/clients/" + client.getExternalId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{externalId}")
    public ClientResponse getClient(@PathVariable String externalId) {
        Client client = clientService.getByExternalId(externalId);
        return ClientResponse.from(client);
    }

    @GetMapping("/search")
    public List<ClientResponse> searchClients(@RequestParam String name) {
        return clientService.searchByName(name)
                .stream()
                .map(ClientResponse::from)
                .toList();
    }
}