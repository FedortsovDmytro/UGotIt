package com.example.demo.controller;

import com.example.demo.base.controller.DashboardController;
import com.example.demo.base.dto.ClientDashboardView;
import com.example.demo.base.entity.Client;
import com.example.demo.base.entity.ClientStatus;
import com.example.demo.base.repository.RiskAssessmentRepository;
import com.example.demo.base.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DashboardControllerTest {

    @Mock
    private ClientService clientService;

    @Mock
    private RiskAssessmentRepository riskRepository;

    @Mock
    private Model model;

    private DashboardController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new DashboardController(clientService, riskRepository);
    }


    @Test
    void testDashboard_emptyList() {
        when(clientService.getDashboardItems(any(Sort.class))).thenReturn(List.of());

        String template = controller.dashboard(0, 10, "id", "asc", model);

        assertEquals("dashboard", template);

        verify(model).addAttribute("items", List.of());
        verify(model).addAttribute("totalItems", 0);
        verify(model).addAttribute("activeItems", 0L);
        verify(model).addAttribute("blockedItems", 0L);
        verify(model).addAttribute("currentPage", 0);
        verify(model).addAttribute("totalPages", 0);
    }

    @Test
    void testShowImportClientsPage() {
        String template = controller.showImportClientsPage();
        assertEquals("upload-files", template);
    }
}