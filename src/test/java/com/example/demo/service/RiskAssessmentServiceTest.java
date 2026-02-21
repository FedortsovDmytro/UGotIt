package com.example.demo.service;


import com.example.demo.base.entity.*;
import com.example.demo.base.repository.RiskAssessmentRepository;
import com.example.demo.base.service.*;
import com.example.demo.risk.RiskAssessmentResult;
import com.example.demo.risk.RiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RiskAssessmentServiceTest {

    private RiskAssessmentService service;
    private RiskAssessmentRepository repository;
    private CreditLimitService creditLimitService;
    private ReceivableAgingService agingService;
    private BisnodeService bisnodeService;
    private ClientService clientService;

    private Client client;

    @BeforeEach
    void setUp() {
        repository = mock(RiskAssessmentRepository.class);
        creditLimitService = mock(CreditLimitService.class);
        agingService = mock(ReceivableAgingService.class);
        bisnodeService = mock(BisnodeService.class);
        clientService = mock(ClientService.class);

        service = new RiskAssessmentService(
                clientService, repository, creditLimitService, agingService, bisnodeService
        );

        client = Client.builder("EXT001", "Test Client", ClientStatus.ACTIVE).build();
    }

    @Test
    void testAssessAndSave() {
        // Mock dependencies
        CreditLimit creditLimit = new CreditLimit();
        ReceivableAging aging = new ReceivableAging();
        Bisnode bisnode = new Bisnode();

        when(creditLimitService.findByClient(client)).thenReturn(creditLimit);
        when(agingService.findCurrentByClient(client)).thenReturn(aging);
        when(bisnodeService.findLatestByClient(client)).thenReturn(bisnode);

        ArgumentCaptor<RiskAssessment> captor = ArgumentCaptor.forClass(RiskAssessment.class);
        when(repository.save(captor.capture())).thenAnswer(i -> i.getArguments()[0]);

        RiskAssessment ra = service.assessAndSave(client);

        assertNotNull(ra);
        assertEquals(client, ra.getClient());
        assertNotNull(ra.getCalculatedAt());
        assertNotNull(captor.getValue()); // Confirm it was saved
    }

    @Test
    void testCalculateRisk() {
        int score = 85;
        String reasons = "High risk test";
        String recommendation = "Monitor closely";

        when(repository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        RiskAssessment ra = service.calculateRisk(client, score, reasons, recommendation);

        assertEquals(client, ra.getClient());
        assertEquals(reasons, ra.getReasons());
        assertEquals(recommendation, ra.getRecommendation());
        assertNotNull(ra.getCalculatedAt());
    }

    @Test
    void testGetForClient() {
        RiskAssessment ra1 = mock(RiskAssessment.class);
        RiskAssessment ra2 = mock(RiskAssessment.class);
        when(repository.findByClient(client)).thenReturn(List.of(ra1, ra2));

        List<RiskAssessment> result = service.getForClient(client);
        assertEquals(2, result.size());
        verify(repository).findByClient(client);
    }

    @Test
    void testGetLatestForClient() {
        RiskAssessment ra1 = mock(RiskAssessment.class);
        RiskAssessment ra2 = mock(RiskAssessment.class);
        when(repository.findAllByClientOrderByCalculatedAtDesc(client)).thenReturn(List.of(ra2, ra1));

        Optional<RiskAssessment> latest = service.getLatestForClient(client);

        assertTrue(latest.isPresent());
        assertEquals(ra2, latest.get());
    }

    @Test
    void testRecalculate() {
        RiskAssessment ra = mock(RiskAssessment.class);
        when(repository.save(any())).thenReturn(ra);

        service.recalculate(client);

        verify(repository).deleteByClient(client);
        verify(repository).save(any(RiskAssessment.class));
    }

    @Test
    void testCalculateForAllClients() {
        Client client2 = Client.builder("EXT002", "Client2", ClientStatus.ACTIVE).build();
        when(clientService.getAll()).thenReturn(List.of(client, client2));
        when(creditLimitService.findByClient(any())).thenReturn(new CreditLimit());
        when(agingService.findCurrentByClient(any())).thenReturn(new ReceivableAging());
        when(bisnodeService.findLatestByClient(any())).thenReturn(new Bisnode());
        when(repository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        service.calculateForAllClients();

        verify(repository, times(2)).save(any(RiskAssessment.class));
        verify(creditLimitService, times(2)).findByClient(any());
    }
}