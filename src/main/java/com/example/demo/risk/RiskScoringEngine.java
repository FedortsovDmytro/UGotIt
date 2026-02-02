package com.example.demo.risk;

import java.util.Set;
import java.util.stream.Collectors;

public class RiskScoringEngine {

    public RiskAssessmentResult assess(Set<RiskSignal> signals) {

        int score = signals.stream()
                .mapToInt(RiskSignal::getWeight)
                .sum();


        RiskLevel level = resolveLevel(score);


        String reasons = buildReasons(signals);


        String recommendation = buildRecommendation(level, signals);


        return new RiskAssessmentResult(score, level, signals, reasons, recommendation);
    }

    private RiskLevel resolveLevel(int score) {
        if (score >= 80) return RiskLevel.CRITICAL;
        if (score >= 60) return RiskLevel.HIGH;
        if (score >= 30) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    private String buildReasons(Set<RiskSignal> signals) {
        if (signals == null || signals.isEmpty()) return "Brak istotnych sygnałów ryzyka.";
        return signals.stream()
                .map(RiskSignal::getDescription)
                .collect(Collectors.joining(", "));
    }

    private String buildRecommendation(RiskLevel level, Set<RiskSignal> signals) {
        return switch (level) {
            case LOW -> "Brak działań. Kontynuować standardowy monitoring.";
            case MEDIUM -> "Zalecany kontakt z klientem i obserwacja trendów. Rozważyć korektę limitu.";
            case HIGH -> "Zalecane działania prewencyjne: redukcja limitu, skrócenie terminu płatności, eskalacja do działu kredytowego.";
            case CRITICAL -> "Uwaga CRITICAL: natychmiastowe działania wymagane!";
        };
    }
}
