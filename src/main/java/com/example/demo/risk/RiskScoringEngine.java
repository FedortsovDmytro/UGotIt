
package com.example.demo.risk;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class RiskScoringEngine {

    public RiskAssessmentResult assess(List<RiskSignal> signals, Locale locale) {

        int score = signals.stream()
                .mapToInt(RiskSignal::getWeight)
                .sum();

        RiskLevel level = resolveLevel(score);

        String reasons = buildReasons(signals, locale);

        String recommendation = buildRecommendation(level, locale);

        return new RiskAssessmentResult(score, level, signals, reasons, recommendation);
    }

    private RiskLevel resolveLevel(int score) {
        if (score >= 80) return RiskLevel.CRITICAL;
        if (score >= 60) return RiskLevel.HIGH;
        if (score >= 30) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    private String buildReasons(List<RiskSignal> signals, Locale locale) {
        if (signals == null || signals.isEmpty()) {
            // Default message in EN/PL
            return locale != null && locale.getLanguage().equals("pl")
                    ? "Brak istotnych sygnałów ryzyka."
                    : "No significant risk signals.";
        }

        return signals.stream()
                .map(signal -> signal.getDescription(locale))
                .distinct()
                .collect(Collectors.joining(", "));
    }

    private String buildRecommendation(RiskLevel level, Locale locale) {
        boolean isPolish = locale != null && locale.getLanguage().equals("pl");

        return switch (level) {
            case LOW -> isPolish
                    ? "Brak działań. Kontynuować monitoring."
                    : "No action required. Continue monitoring.";
            case MEDIUM -> isPolish
                    ? "Monitorować klienta i rozważyć korektę limitu."
                    : "Monitor the client and consider limit adjustment.";
            case HIGH -> isPolish
                    ? "Zalecana redukcja limitu i działania prewencyjne."
                    : "Recommended limit reduction and preventive actions.";
            case CRITICAL -> isPolish
                    ? "Natychmiastowa blokada i analiza kredytowa!"
                    : "Immediate block and credit analysis required!";
        };
    }
}
