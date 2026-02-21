package com.example.demo.base.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UploadResult {

    private String clientId;
    private String clientName;
    private String riskLevel;
    private Integer riskScore;
    private String reasons;
    private String recommendation;
    private LocalDateTime calculationDate;
    private BigDecimal creditLimit;
    private BigDecimal exposure;
    private Integer averageDaysPastDue;
    public UploadResult(String clientName, String clientId, BigDecimal creditLimit,
                        BigDecimal totalExposure, int maxDaysOverdue, int riskScore,
                        String riskLevel, String reasons, String recommendation,
                        LocalDateTime calculatedAt) {
        this.clientName = clientName;
        this.clientId = clientId;
        this.creditLimit = creditLimit;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.reasons = reasons;
        this.recommendation = recommendation;
        this.calculationDate = calculatedAt;
        this.averageDaysPastDue = maxDaysOverdue;
        this.exposure = totalExposure;
        this.riskScore = riskScore;
        this.clientId = clientId;
        this.riskLevel = riskLevel;
        this.reasons = reasons;
        this.recommendation = recommendation;


    }
    public UploadResult(){
    }
    public String getClientName() { return clientName; }
    public String getClientId() { return clientId; }
    public BigDecimal getCreditLimit() { return creditLimit; }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public void setReasons(String reasons) {
        this.reasons = reasons;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public void setCalculationDate(LocalDateTime calculationDate) {
        this.calculationDate = calculationDate;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public void setExposure(BigDecimal exposure) {
        this.exposure = exposure;
    }

    public void setAverageDaysPastDue(Integer averageDaysPastDue) {
        this.averageDaysPastDue = averageDaysPastDue;
    }

    public int getRiskScore() { return riskScore; }
    public String getRiskLevel() { return riskLevel; }
    public String getReasons() { return reasons; }
    public String getRecommendation() { return recommendation; }

    public LocalDateTime getCalculationDate() {
        return calculationDate;
    }

    public BigDecimal getExposure() {
        return exposure;
    }

    public Integer getAverageDaysPastDue() {
        return averageDaysPastDue;
    }
}