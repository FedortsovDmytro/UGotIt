//package com.example.demo.service;
//
//import com.example.demo.base.entity.ClientStatus;
//import com.example.demo.base.entity.Client;
//import com.example.demo.base.entity.RiskAssessment;
//import com.example.demo.base.repository.RiskAssessmentRepository;
//import com.example.demo.base.service.RiskAssessmentService;
//import com.example.demo.risk.RiskLevel;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//
//import java.time.LocalDateTime;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.*;
//
//class RiskAssessmentServiceTest {
//
//    private RiskAssessmentRepository riskAssessmentRepository;
//    private RiskAssessmentService riskAssessmentService;
//    private Client client = Client.builder("ext-1", "Alice", ClientStatus.ACTIVE).build();
//
//    @BeforeEach
//    void setUp() {
//        riskAssessmentRepository = Mockito.mock(RiskAssessmentRepository.class);
//        riskAssessmentService = new RiskAssessmentService(riskAssessmentRepository);
//    }
//
//    @Test
//    void calculateRisk_shouldReturnSavedRiskAssessment() {
//        RiskAssessment ra = new RiskAssessment.Builder(client, 70, RiskLevel.MEDIUM, LocalDateTime.now())
//                .reasons("Some reasons")
//                .recommendation("Some recommendation")
//                .build();
//
//        when(riskAssessmentRepository.save(any(RiskAssessment.class))).thenReturn(ra);
//
//        RiskAssessment result = riskAssessmentService.calculateRisk(client, 70, "Some reasons", "Some recommendation");
//
//        assertThat(result.getRiskLevel()).isEqualTo(RiskLevel.MEDIUM);
//        verify(riskAssessmentRepository, times(1)).save(any(RiskAssessment.class));
//    }
//}
