package com.example.demo.service;

import com.example.demo.entity.Client;
import com.example.demo.entity.RiskAssessment;
import com.example.demo.entity.RiskLevel;
import com.example.demo.repository.RiskAssessmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RiskAssessmentServiceTest {

    private RiskAssessmentRepository riskAssessmentRepository;
    private RiskAssessmentService riskAssessmentService;
    private Client client = Client.builder("ext-1", "Alice", com.example.demo.entity.ClientStatus.ACTIVE).build();

    @BeforeEach
    void setUp() {
        riskAssessmentRepository = Mockito.mock(RiskAssessmentRepository.class);
        riskAssessmentService = new RiskAssessmentService(riskAssessmentRepository);
    }

    @Test
    void calculateRisk_shouldReturnSavedRiskAssessment() {
        RiskAssessment ra = new RiskAssessment.Builder(client, 70, RiskLevel.MEDIUM)
                .reasons("Some reasons")
                .recommendation("Some recommendation")
                .build();

        when(riskAssessmentRepository.save(any(RiskAssessment.class))).thenReturn(ra);

        RiskAssessment result = riskAssessmentService.calculateRisk(client, 70, "Some reasons", "Some recommendation");

        assertThat(result.getRiskLevel()).isEqualTo(RiskLevel.MEDIUM);
        verify(riskAssessmentRepository, times(1)).save(any(RiskAssessment.class));
    }
}
