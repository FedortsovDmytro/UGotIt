package com.example.demo.risk;

import com.example.demo.base.entity.Bisnode;
import com.example.demo.base.entity.Client;
import com.example.demo.base.entity.CreditLimit;
import com.example.demo.base.entity.ReceivableAging;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

@Component
public class RiskScoreCalculator {

    private final RiskScoringEngine engine = new RiskScoringEngine();

    public RiskAssessmentResult assess(
            Client client,
            CreditLimit creditLimit,
            ReceivableAging aging,
            Bisnode bisnode
    ) {

        Set<RiskSignal> signals = EnumSet.noneOf(RiskSignal.class);

        addOverdueSignals(aging, signals);
        addLimitUsageSignals(creditLimit, signals);
        addExternalRatingSignals(bisnode, signals);
        addNewClientSignal(client, signals);
        return engine.assess(signals);
    }

    private void addOverdueSignals(ReceivableAging a, Set<RiskSignal> signals) {
        if (a == null) return;
        if (gtZero(a.getOverdueAbove360())) signals.add(RiskSignal.OVERDUE_60_PLUS);
        else if (gtZero(a.getOverdue31to60())) signals.add(RiskSignal.OVERDUE_31_60);
        else if (gtZero(a.getOverdue15to30())) signals.add(RiskSignal.OVERDUE_15_30);
        else if (gtZero(a.getOverdue8to14())) signals.add(RiskSignal.OVERDUE_8_14);
        else if (gtZero(a.getOverdue1to7())) signals.add(RiskSignal.OVERDUE_1_7);
    }

    private void addLimitUsageSignals(CreditLimit cl, Set<RiskSignal> signals) {
        if (cl == null || cl.getLimitAmount() == null) return;

        BigDecimal usage = cl.getUsedAmount()
                .divide(cl.getLimitAmount(), 2, BigDecimal.ROUND_HALF_UP);

        if (usage.compareTo(new BigDecimal("0.95")) > 0) signals.add(RiskSignal.LIMIT_USAGE_95);
        else if (usage.compareTo(new BigDecimal("0.85")) > 0) signals.add(RiskSignal.LIMIT_USAGE_85);
        else if (usage.compareTo(new BigDecimal("0.70")) > 0) signals.add(RiskSignal.LIMIT_USAGE_70);
    }

    private void addExternalRatingSignals(Bisnode b, Set<RiskSignal> signals) {
        if (b == null || b.getRating() == null) return;

        switch (b.getRating().toUpperCase()) {
            case "D" -> signals.add(RiskSignal.EXTERNAL_RATING_D);
            case "C" -> signals.add(RiskSignal.EXTERNAL_RATING_C);
        }
    }

    private void addNewClientSignal(Client client, Set<RiskSignal> signals) {
        if (client == null || client.getCreatedAt() == null) return;

        if (client.getCreatedAt().isAfter(LocalDate.now().minusMonths(6))) {
            signals.add(RiskSignal.NEW_CLIENT);
        }
    }

    private boolean gtZero(BigDecimal val) {
        return val != null && val.compareTo(BigDecimal.ZERO) > 0;
    }
}
