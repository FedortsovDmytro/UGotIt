package com.example.demo.service;

import com.example.demo.base.entity.Client;
import com.example.demo.base.entity.ClientStatus;
import com.example.demo.base.repository.ClientRepository;
import com.example.demo.base.service.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class ClientServiceIntegrationTest {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientRepository clientRepository;

    @Test
    void testCreateAndFetchClient() {
        Client client = clientService.createClient("ext123", "John Doe", ClientStatus.ACTIVE);

        Client fromDb = clientService.getByExternalId("ext123");
        assertThat(fromDb).isNotNull();
        assertThat(fromDb.getName()).isEqualTo("John Doe");

        List<Client> searchResult = clientService.searchByName("john");
        assertThat(searchResult).hasSize(1);
        assertThat(searchResult.get(0).getExternalId()).isEqualTo("ext123");
    }
}
