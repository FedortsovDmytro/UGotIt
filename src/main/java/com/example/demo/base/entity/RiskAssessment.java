package com.example.demo.base.entity;

import com.example.demo.risk.RiskLevel;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "risk_assessment")
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Client client;

    public Set<RiskSignalEntity> getSignals() {
        return signals;
    }

    @Column(nullable = false)
    private int riskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;

    @Column(columnDefinition = "TEXT")
    private String reasons;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    @Column(nullable = false)
    private LocalDateTime calculatedAt;
    @OneToMany(mappedBy = "riskAssessment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RiskSignalEntity> signals;

    public RiskAssessment() {}

    private RiskAssessment(Builder b) {
        this.client = b.client;
        this.riskScore = b.riskScore;
        this.riskLevel = b.riskLevel;
        this.reasons = b.reasons;
        this.recommendation = b.recommendation;
        this.calculatedAt = b.calculatedAt;
    }



    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public void setReasons(String reasons) {
        this.reasons = reasons;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public static class Builder {
        private final Client client;
        private final int riskScore;
        private final RiskLevel riskLevel;
        private final LocalDateTime calculatedAt;

        private String reasons;
        private String recommendation;

        public Builder(Client client, int riskScore,
                       RiskLevel riskLevel ,LocalDateTime calculatedAt) {
            this.client = client;
            this.riskScore = riskScore;
            this.riskLevel = riskLevel;
            this.calculatedAt = calculatedAt;

        }

        public Builder reasons(String val) {
            this.reasons = val;
            return this;
        }

        public Builder recommendation(String val) {
            this.recommendation = val;
            return this;
        }

        public RiskAssessment build() {
            if (client == null) throw new IllegalStateException("client required");
            if (riskLevel == null)
                throw new IllegalStateException("riskLevel required");

            return new RiskAssessment(this);
        }
    }

    public void setSignals(Set<RiskSignalEntity> signals) {
        this.signals = signals;
    }

    public Long getId() {
        return id;
    }

    public Client getClient() {
        return client;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public String getReasons() {
        return reasons;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }



}
