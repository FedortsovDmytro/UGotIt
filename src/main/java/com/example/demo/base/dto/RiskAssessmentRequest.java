package com.example.demo.base.base.dto;

public record RiskAssessmentRequest(
        Long clientId,
        int score,
        String reasons,
        String recommendation
) {}
