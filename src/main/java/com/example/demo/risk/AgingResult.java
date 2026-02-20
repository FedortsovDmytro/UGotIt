package com.example.demo.base.risk;


import java.math.BigDecimal;

public class AgingResult {

    private BigDecimal current = BigDecimal.ZERO;
    private BigDecimal overdue1to7 = BigDecimal.ZERO;
    private BigDecimal overdue8to14 = BigDecimal.ZERO;
    private BigDecimal overdue15to30 = BigDecimal.ZERO;
    private BigDecimal overdue31to60 = BigDecimal.ZERO;
    private BigDecimal overdue60plus = BigDecimal.ZERO;

    public void addCurrent(BigDecimal amount) {
        current = current.add(amount);
    }

    public void add1to7(BigDecimal amount) {
        overdue1to7 = overdue1to7.add(amount);
    }

    public void add8to14(BigDecimal amount) {
        overdue8to14 = overdue8to14.add(amount);
    }

    public void add15to30(BigDecimal amount) {
        overdue15to30 = overdue15to30.add(amount);
    }

    public void add31to60(BigDecimal amount) {
        overdue31to60 = overdue31to60.add(amount);
    }

    public void add60Plus(BigDecimal amount) {
        overdue60plus = overdue60plus.add(amount);
    }


    public boolean hasOverdue1to7() {
        return overdue1to7.signum() > 0;
    }

    public boolean hasOverdue15to30() {
        return overdue15to30.signum() > 0;
    }

    public boolean hasOverdue31to60() {
        return overdue31to60.signum() > 0;
    }

    public boolean hasOverdue60Plus() {
        return overdue60plus.signum() > 0;
    }

    public BigDecimal getTotalOverdue() {
        return overdue1to7
                .add(overdue8to14)
                .add(overdue15to30)
                .add(overdue31to60)
                .add(overdue60plus);
    }
}
