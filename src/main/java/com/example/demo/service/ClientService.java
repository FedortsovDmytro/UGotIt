package com.example.demo.service;

import com.example.demo.entity.Client;
import com.example.demo.entity.ClientStatus;
import com.example.demo.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {
    private final ClientRepository clientRepository;

    ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }
    public Client createClient(String externalId, String fullName, ClientStatus status) {
        Client client = new Client.Builder(externalId, fullName, status).build();
        return clientRepository.save(client);
    }

    public List<Client> searchByName(String name) {
        return clientRepository.findByFullNameContainingIgnoreCase(name);
    }

    public Client getByExternalId(String externalId) {
        return clientRepository.findByExternalId(externalId)
                .orElseThrow(() -> new IllegalStateException("Client not found"));
    }
    public Client getClient(Long id) {
    return clientRepository.findById(id).orElseThrow(() -> new IllegalStateException("Client not found"));
    }
}
