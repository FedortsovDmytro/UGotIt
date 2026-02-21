package com.example.demo.service;

import com.example.demo.base.dto.ClientDashboardRow;
import com.example.demo.base.entity.Client;
import com.example.demo.base.entity.ClientStatus;
import com.example.demo.base.entity.RiskAssessment;
import com.example.demo.base.repository.ClientDashboardRepository;
import com.example.demo.base.repository.ClientRepository;
import com.example.demo.base.service.ClientService;
import com.example.demo.risk.RiskLevel;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientDashboardRepository dashboardRepository;

    private ClientService clientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        clientService = new ClientService(clientRepository, dashboardRepository);
    }

    @Test
    void shouldCreateClient() {
        Client client = new Client.Builder("ext123", "John Doe", ClientStatus.ACTIVE).build();
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        Client result = clientService.createClient("ext123", "John Doe", ClientStatus.ACTIVE);

        assertThat(result.getName()).isEqualTo("John Doe");
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void shouldGetByExternalId() {
        Client client = new Client.Builder("ext123", "John Doe", ClientStatus.ACTIVE).build();
        when(clientRepository.findByExternalId("ext123")).thenReturn(Optional.of(client));

        Client result = clientService.getByExternalId("ext123");

        assertThat(result).isNotNull();
        assertThat(result.getExternalId()).isEqualTo("ext123");
    }

    @Test
    void shouldThrowWhenExternalIdNotFound() {
        when(clientRepository.findByExternalId("missing")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> clientService.getByExternalId("missing"));
    }

    @Test
    void shouldSearchByName() {
        Client client1 = new Client.Builder("ext1", "Alice Smith", ClientStatus.ACTIVE).build();
        Client client2 = new Client.Builder("ext2", "Bob Smith", ClientStatus.ACTIVE).build();
        when(clientRepository.findByFullNameContainingIgnoreCase("Smith")).thenReturn(List.of(client1, client2));

        List<Client> results = clientService.searchByName("Smith");

        assertThat(results).hasSize(2);
        verify(clientRepository, times(1)).findByFullNameContainingIgnoreCase("Smith");
    }

    @Test
    void shouldImportFromCsv() throws Exception {
        String csvContent = "Name,ExternalId,Bisnode\nJohn,ext123,AAA\nJane,ext456,BBB\n";
        MockMultipartFile file = new MockMultipartFile("file", "clients.csv", "text/csv", csvContent.getBytes());

        clientService.importFromCsv(file);

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository, times(2)).save(captor.capture());

        List<Client> savedClients = captor.getAllValues();
        assertThat(savedClients).extracting(Client::getName).containsExactly("John", "Jane");
    }

    @Test
    void shouldSortDashboardItems() {
        Client client1 = new Client.Builder("ext1", "Alice", ClientStatus.ACTIVE).build();
        Client client2 = new Client.Builder("ext2", "Bob", ClientStatus.ACTIVE).build();

        RiskAssessment risk1 = new RiskAssessment();
        risk1.setRiskLevel(RiskLevel.LOW);
        RiskAssessment risk2 = new RiskAssessment();
        risk2.setRiskLevel(RiskLevel.HIGH);

        when(dashboardRepository.fetchDashboardRaw())
                .thenReturn(List.of(
                        new Object[]{client1, risk1},
                        new Object[]{client2, risk2}
                ));

        List<ClientDashboardRow> dashboard = clientService.getDashboardItems(Sort.by(Sort.Direction.ASC, "name"));

        assertThat(dashboard.get(0).getClient().getName()).isEqualTo("Alice");
        assertThat(dashboard.get(1).getClient().getName()).isEqualTo("Bob");
    }

    @Test
    void shouldFindAllWithPageable() {
        Pageable pageable = mock(Pageable.class);
        Page<Client> page = mock(Page.class);
        when(clientRepository.findAll(pageable)).thenReturn(page);

        Page<Client> result = clientService.findAll(pageable);

        assertThat(result).isEqualTo(page);
        verify(clientRepository).findAll(pageable);
    }
}