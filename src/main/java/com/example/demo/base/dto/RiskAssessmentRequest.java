package com.example.demo.dto;

public record RiskAssessmentRequest(
        Long clientId,
        int score,
        String reasons,
        String recommendation
) {}
