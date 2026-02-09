package com.example.demo.risk;
public enum RiskLevel {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int priority;

    RiskLevel(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
