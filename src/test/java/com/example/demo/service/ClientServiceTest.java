//package com.example.demo.service;
//
//import com.example.demo.base.entity.Client;
//import com.example.demo.base.entity.ClientStatus;
//import com.example.demo.base.repository.ClientRepository;
//import com.example.demo.base.service.ClientService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.*;
//
//class ClientServiceTest {
//
//    private ClientRepository clientRepository;
//    private ClientService clientService;
//
//    @BeforeEach
//    void setUp() {
//        clientRepository = Mockito.mock(ClientRepository.class);
//        clientService = new ClientService(clientRepository);
//    }
//
//    @Test
//    void createClient_shouldSaveClient() {
//        Client client = Client.builder("ext-1", "Test Client", ClientStatus.ACTIVE).build();
//        when(clientRepository.save(any(Client.class))).thenReturn(client);
//
//        Client result = clientService.createClient("ext-1", "Test Client", ClientStatus.ACTIVE);
//
//        assertThat(result.getExternalId()).isEqualTo("ext-1");
//        verify(clientRepository, times(1)).save(any(Client.class));
//    }
//
//    @Test
//    void searchByName_shouldReturnClients() {
//        Client client = Client.builder("ext-1", "Alice", ClientStatus.ACTIVE).build();
//        when(clientRepository.findByFullNameContainingIgnoreCase("Alice")).thenReturn(List.of(client));
//
//        List<Client> result = clientService.searchByName("Alice");
//
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).getName()).isEqualTo("Alice");
//    }
//
//    @Test
//    void getByExternalId_shouldReturnClient() {
//        Client client = Client.builder("ext-1", "Alice", ClientStatus.ACTIVE).build();
//        when(clientRepository.findByExternalId("ext-1")).thenReturn(Optional.of(client));
//
//        Client result = clientService.getByExternalId("ext-1");
//
//        assertThat(result.getName()).isEqualTo("Alice");
//    }
//}
