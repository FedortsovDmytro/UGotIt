package com.example.demo.risk;

import java.util.Set;

public class RiskAssessmentResult {

    private final int score;
    private final RiskLevel level;
    private final Set<RiskSignal> signals;
    private final String reasons;
    private final String recommendation;

    public RiskAssessmentResult(
            int score,
            RiskLevel level,
            Set<RiskSignal> signals,
            String reasons,
            String recommendation
    ) {
        this.score = score;
        this.level = level;
        this.signals = signals;
        this.reasons = reasons;
        this.recommendation = recommendation;
    }

    public int getScore() {
        return score;
    }

    public RiskLevel getLevel() {
        return level;
    }

    public Set<RiskSignal> getSignals() {
        return signals;
    }

    public String getReasons() {
        return reasons;
    }

    public String getRecommendation() {
        return recommendation;
    }
}
