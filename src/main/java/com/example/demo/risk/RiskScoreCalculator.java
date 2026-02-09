package com.example.demo.risk;

import com.example.demo.base.entity.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class RiskScoreCalculator {

    private static final BigDecimal LOW_EXPOSURE = new BigDecimal("10000");
    private static final BigDecimal MEDIUM_EXPOSURE = new BigDecimal("100000");

    private final RiskScoringEngine engine = new RiskScoringEngine();

    public RiskAssessmentResult assess(
            Client client,
            CreditLimit creditLimit,
            ReceivableAging aging,
            Bisnode bisnode
    ) {

        List<RiskSignal> signals = new ArrayList<>();

        addOverdueSignals(aging, signals);
        addLimitUsageSignals(creditLimit, signals);
        addExternalRatingSignals(bisnode, signals);
        addNewClientSignal(client, signals);
        addMissingDataSignals(creditLimit, aging, bisnode, signals);

        return engine.assess(signals);
    }

    private void addOverdueSignals(ReceivableAging a, List<RiskSignal> signals) {
        if (a == null) return;

        addWeighted(a.getOverdue1to7(), RiskSignal.OVERDUE_1_7, signals);
        addWeighted(a.getOverdue8to14(), RiskSignal.OVERDUE_8_14, signals);
        addWeighted(a.getOverdue15to30(), RiskSignal.OVERDUE_15_30, signals);
        addWeighted(a.getOverdue31to60(), RiskSignal.OVERDUE_31_60, signals);
        addWeighted(a.getOverdueAbove360(), RiskSignal.OVERDUE_60_PLUS, signals);
    }

    private void addWeighted(BigDecimal amount, RiskSignal signal, List<RiskSignal> signals) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            return;

        int multiplier = amount
                .divide(new BigDecimal("50000"), 0, RoundingMode.UP)
                .intValue();

        for (int i = 0; i < multiplier; i++) {
            signals.add(signal);
        }
    }

    // ===== LIMIT USAGE =====
    private void addLimitUsageSignals(CreditLimit cl, List<RiskSignal> signals) {
        if (cl == null || cl.getLimitAmount() == null || cl.getUsedAmount() == null)
            return;

        BigDecimal usage = cl.getUsedAmount()
                .divide(cl.getLimitAmount(), 2, RoundingMode.HALF_UP);

        if (usage.compareTo(new BigDecimal("0.95")) > 0)
            signals.add(RiskSignal.LIMIT_USAGE_95);
        else if (usage.compareTo(new BigDecimal("0.85")) > 0)
            signals.add(RiskSignal.LIMIT_USAGE_85);
        else if (usage.compareTo(new BigDecimal("0.70")) > 0)
            signals.add(RiskSignal.LIMIT_USAGE_70);
    }

    // ===== EXTERNAL RATING =====
    private void addExternalRatingSignals(Bisnode b, List<RiskSignal> signals) {
        if (b == null || b.getRating() == null)
            return;

        switch (b.getRating().toUpperCase()) {
            case "D" -> signals.add(RiskSignal.EXTERNAL_RATING_D);
            case "C" -> signals.add(RiskSignal.EXTERNAL_RATING_C);
        }
    }

    // ===== NEW CLIENT =====
    private void addNewClientSignal(Client client, List<RiskSignal> signals) {
        if (client == null || client.getCreatedAt() == null)
            return;

        if (client.getCreatedAt().isAfter(LocalDate.now().minusMonths(6))) {
            signals.add(RiskSignal.NEW_CLIENT);
        }
    }

    // ===== MISSING DATA =====
    private void addMissingDataSignals(
            CreditLimit creditLimit,
            ReceivableAging aging,
            Bisnode bisnode,
            List<RiskSignal> signals
    ) {

        if (aging == null) {
            signals.add(RiskSignal.NO_AGING_DATA);
            return;
        }

        BigDecimal exposure = aging.getTotalAmount() != null
                ? aging.getTotalAmount()
                : BigDecimal.ZERO;

        if (creditLimit == null) {
            if (exposure.compareTo(LOW_EXPOSURE) < 0)
                signals.add(RiskSignal.NO_CREDIT_LIMIT_LOW_EXPOSURE);
            else if (exposure.compareTo(MEDIUM_EXPOSURE) < 0)
                signals.add(RiskSignal.NO_CREDIT_LIMIT_MEDIUM_EXPOSURE);
            else
                signals.add(RiskSignal.NO_CREDIT_LIMIT_HIGH_EXPOSURE);
        }

        if (bisnode == null)
            signals.add(RiskSignal.NO_EXTERNAL_RATING);
    }
}
