package com.example.demo.calculator;

import com.example.demo.base.entity.CreditLimit;
import com.example.demo.base.entity.ReceivableAging;
import com.example.demo.risk.RiskScoreCalculator;
import com.example.demo.risk.RiskSignal;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RiskScoreCalculatorTest {

    private final RiskScoreCalculator calculator = new RiskScoreCalculator();

    @Test
    void testAddLimitUsageSignals_belowThreshold() {
        CreditLimit cl = new CreditLimit();
        cl.setLimitAmount(new BigDecimal("100"));
        cl.setUsedAmount(new BigDecimal("50"));

        List<RiskSignal> signals = new ArrayList<>();
        calculator.addLimitUsageSignals(cl, signals);

        // Очікуємо, що сигналів не буде
        assertTrue(signals.isEmpty());
    }

    @Test
    void testAddLimitUsageSignals_70Percent() {
        CreditLimit cl = new CreditLimit();
        cl.setLimitAmount(new BigDecimal("100"));
        cl.setUsedAmount(new BigDecimal("80"));

        List<RiskSignal> signals = new ArrayList<>();
        calculator.addLimitUsageSignals(cl, signals);

        assertTrue(signals.contains(RiskSignal.LIMIT_USAGE_70));
    }

    @Test
    void testAddLimitUsageSignals_85Percent() {
        CreditLimit cl = new CreditLimit();
        cl.setLimitAmount(new BigDecimal("100"));
        cl.setUsedAmount(new BigDecimal("90"));

        List<RiskSignal> signals = new ArrayList<>();
        calculator.addLimitUsageSignals(cl, signals);

        assertTrue(signals.contains(RiskSignal.LIMIT_USAGE_85));
    }

    @Test
    void testAddLimitUsageSignals_95Percent() {
        CreditLimit cl = new CreditLimit();
        cl.setLimitAmount(new BigDecimal("100"));
        cl.setUsedAmount(new BigDecimal("96"));

        List<RiskSignal> signals = new ArrayList<>();
        calculator.addLimitUsageSignals(cl, signals);

        assertTrue(signals.contains(RiskSignal.LIMIT_USAGE_95));
    }

    @Test
    void testAddLimitUsageSignals_limitZero() {
        CreditLimit cl = new CreditLimit();
        cl.setLimitAmount(BigDecimal.ZERO);
        cl.setUsedAmount(new BigDecimal("50"));

        List<RiskSignal> signals = new ArrayList<>();
        calculator.addLimitUsageSignals(cl, signals);

        assertTrue(signals.contains(RiskSignal.LIMIT_USAGE_95));
    }
    @Test
    void testAddOverdueSignals() {
        ReceivableAging aging = new ReceivableAging();
        aging.setOverdue1to7(new BigDecimal("60000"));
        aging.setOverdue8to14(BigDecimal.ZERO);

        List<RiskSignal> signals = new ArrayList<>();
        calculator.addOverdueSignals(aging, signals);

        long count = signals.stream().filter(s -> s == RiskSignal.OVERDUE_1_7).count();
        assertTrue(count == 2);
    }
}