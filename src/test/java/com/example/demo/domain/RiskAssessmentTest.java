//package com.example.demo.domain;
//
//import com.example.demo.base.entity.Client;
//import com.example.demo.base.entity.ClientStatus;
//import com.example.demo.base.entity.RiskAssessment;
//import com.example.demo.risk.RiskLevel;
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDateTime;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class RiskAssessmentTest {
//
//    @Test
//    void shouldBuildRiskAssessmentCorrectly() {
//        Client client = new Client.Builder("ext1", "Client1", ClientStatus.ACTIVE).build();
//
//        RiskAssessment ra = new RiskAssessment.Builder(client, 75, RiskLevel.MEDIUM, LocalDateTime.now())
//                .reasons("Late payments")
//                .recommendation("Reduce limit")
//                .build();
//
//        assertEquals(client, ra.getClient());
//        assertEquals(75, ra.getRiskScore());
//        assertEquals(RiskLevel.MEDIUM, ra.getRiskLevel());
//        assertEquals("Late payments", ra.getReasons());
//        assertEquals("Reduce limit", ra.getRecommendation());
//        assertNotNull(ra.getCalculatedAt());
//    }
//
//    @Test
//    void shouldThrowIfRequiredFieldsMissing() {
//        Client client = new Client.Builder("ext1", "Client1", ClientStatus.ACTIVE).build();
//
//        assertThrows(IllegalStateException.class,
//                () -> new RiskAssessment.Builder(null, 50, RiskLevel.LOW,LocalDateTime.now()).build());
//
//        assertThrows(IllegalStateException.class,
//                () -> new RiskAssessment.Builder(client, 50, null,LocalDateTime.now()).build());
//    }
//}
