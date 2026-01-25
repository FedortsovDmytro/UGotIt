package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.service.ClientService;
import com.example.demo.service.InvoiceService;
import com.example.demo.service.PaymentService;
import com.example.demo.service.RiskAssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;


import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(RiskAssessmentController.class)
class RiskAssessmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RiskAssessmentService riskAssessmentService;

    @MockitoBean
    private ClientService clientService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCalculateRisk() throws Exception {
        Client client = Client.builder("ext-1", "Alice", ClientStatus.ACTIVE).build();

        RiskAssessment risk = new RiskAssessment.Builder(
                client,
                70,
                RiskLevel.MEDIUM
        )
                .reasons("Test reasons")
                .recommendation("Test recommendation")
                .build();

        when(clientService.getByExternalId("ext-1")).thenReturn(client);
        when(riskAssessmentService.calculateRisk(
                client,
                70,
                "Test reasons",
                "Test recommendation"
        )).thenReturn(risk);

        String body = """
                {
                  "score": 70,
                  "reasons": "Test reasons",
                  "recommendation": "Test recommendation"
                }
                """;

        mockMvc.perform(
                        post("/clients/ext-1/risk-assessments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.riskLevel").value("MEDIUM"));
    }
}
