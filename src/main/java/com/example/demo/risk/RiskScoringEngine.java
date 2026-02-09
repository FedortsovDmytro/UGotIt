package com.example.demo.risk;

import java.util.List;
import java.util.stream.Collectors;

public class RiskScoringEngine {

    public RiskAssessmentResult assess(List<RiskSignal> signals) {

        int score = signals.stream()
                .mapToInt(RiskSignal::getWeight)
                .sum();

        RiskLevel level = resolveLevel(score);

        String reasons = buildReasons(signals);

        String recommendation = buildRecommendation(level);

        return new RiskAssessmentResult(score, level, signals, reasons, recommendation);
    }

    private RiskLevel resolveLevel(int score) {
        if (score >= 80) return RiskLevel.CRITICAL;
        if (score >= 60) return RiskLevel.HIGH;
        if (score >= 30) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    private String buildReasons(List<RiskSignal> signals) {
        if (signals == null || signals.isEmpty())
            return "Brak istotnych sygnałów ryzyka.";

        return signals.stream()
                .map(RiskSignal::getDescription)
                .distinct()
                .collect(Collectors.joining(", "));
    }

    private String buildRecommendation(RiskLevel level) {
        return switch (level) {
            case LOW -> "Brak działań. Kontynuować monitoring.";
            case MEDIUM -> "Monitorować klienta i rozważyć korektę limitu.";
            case HIGH -> "Zalecana redukcja limitu i działania prewencyjne.";
            case CRITICAL -> "Natychmiastowa blokada i analiza kredytowa!";
        };
    }
}
