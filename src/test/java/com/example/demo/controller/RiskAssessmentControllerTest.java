package com.example.demo.controller;

import com.example.demo.base.controller.RiskAssessmentController;
import com.example.demo.base.entity.*;
import com.example.demo.base.service.ClientService;
import com.example.demo.base.service.CreditLimitService;
import com.example.demo.base.service.ReceivableAgingService;
import com.example.demo.base.service.RiskAssessmentService;
import com.example.demo.risk.RiskAssessmentResult;
import com.example.demo.risk.RiskScoringEngine;
import com.example.demo.risk.RiskSignal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RiskAssessmentControllerTest {

    @Mock
    private RiskAssessmentService riskService;

    @Mock
    private ClientService clientService;

    @Mock
    private CreditLimitService creditLimitService;

    @Mock
    private ReceivableAgingService agingService;

    @Mock
    private RiskScoringEngine riskScoringEngine;

    @Mock
    private Model model;

    private RiskAssessmentController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new RiskAssessmentController(
                riskService, clientService, creditLimitService, agingService, riskScoringEngine
        );
    }

    @Test
    void testCalculateRisk_redirectsToDashboard() {
        Client client = new Client();
        when(clientService.getByExternalId("ext123")).thenReturn(client);

        String view = controller.calculateRisk("ext123");

        assertEquals("redirect:/dashboard", view);
        verify(riskService).assessAndSave(client);
    }

    @Test
    void testRecalculateRisk_redirectsToDashboard() {
        Client client = new Client();
        when(clientService.getByExternalId("ext123")).thenReturn(client);

        String view = controller.recalculateRisk("ext123");

        assertEquals("redirect:/dashboard", view);
        verify(riskService).recalculate(client);
    }

    @Test
    void testShowRiskDetails_withSignals() {
        Client client = new Client();
        client.setClientId(String.valueOf(1L));
        client.setExternalId("ext123");

        RiskSignalEntity signalEntity = new RiskSignalEntity();
        signalEntity.setSignal(RiskSignal.LIMIT_USAGE_85);

        RiskAssessment assessment = new RiskAssessment();

        CreditLimit creditLimit = new CreditLimit();
        creditLimit.setLimitAmount(new BigDecimal("1000"));
        creditLimit.setUsedAmount(new BigDecimal("850"));

        ReceivableAging aging = new ReceivableAging();
        aging.setPaymentTermsDays(30);

        when(clientService.getByExternalId("ext123")).thenReturn(client);
        when(riskService.getLatestForClient(client)).thenReturn(Optional.of(assessment));
        when(creditLimitService.findByExternalId("ext123")).thenReturn(Optional.of(creditLimit));
        when(agingService.findByExternalId("ext123")).thenReturn(aging);


        String view = controller.showRiskDetails("ext123", model);

        assertEquals("risk-details", view);
        verify(model).addAttribute("client", client);
        verify(model).addAttribute("risk", assessment);
        verify(model).addAttribute("creditLimit", Optional.of(creditLimit));
        verify(model).addAttribute("aging", aging);

    }

    @Test
    void testShowRiskDetails_noAssessment() {
        Client client = new Client();
        client.setExternalId("ext123");

        when(clientService.getByExternalId("ext123")).thenReturn(client);
        when(riskService.getLatestForClient(client)).thenReturn(Optional.empty());
        when(creditLimitService.findByExternalId("ext123")).thenReturn(Optional.empty());
        when(agingService.findByExternalId("ext123")).thenReturn(null);

        String view = controller.showRiskDetails("ext123", model);

        assertEquals("risk-details", view);
        verify(model).addAttribute("client", client);
        verify(model).addAttribute("risk", null);
        verify(model).addAttribute("creditLimit", Optional.empty());
        verify(model).addAttribute("aging", null);
    }
}