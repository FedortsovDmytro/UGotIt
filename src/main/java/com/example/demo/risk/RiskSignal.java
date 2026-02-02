package com.example.demo.risk;

public enum RiskSignal {

    OVERDUE_1_7(5, "Opóźnienia płatności 1–7 dni"),
    OVERDUE_8_14(10, "Opóźnienia płatności 8–14 dni"),
    OVERDUE_15_30(20, "Opóźnienia płatności 15–30 dni"),
    OVERDUE_31_60(30, "Opóźnienia płatności 31–60 dni"),
    OVERDUE_60_PLUS(40, "Opóźnienia płatności powyżej 60 dni"),

    DSO_TREND_UP_5(5, "Wzrost DSO o 5 dni"),
    DSO_TREND_UP_10(10, "Wzrost DSO o 10 dni"),
    DSO_TREND_UP_15(15, "Wzrost DSO o 15 dni"),

    LIMIT_USAGE_70(10, "Wykorzystanie limitu powyżej 70%"),
    LIMIT_USAGE_85(15, "Wykorzystanie limitu powyżej 85%"),
    LIMIT_USAGE_95(20, "Wykorzystanie limitu powyżej 95%"),

    EXTERNAL_RATING_C(10, "Rating zewnętrzny C"),
    EXTERNAL_RATING_D(15, "Rating zewnętrzny D"),

    NEW_CLIENT(10, "Nowy klient bez historii płatniczej");

    private final int weight;
    private final String description;

    RiskSignal(int weight, String description) {
        this.weight = weight;
        this.description = description;
    }

    public int getWeight() {
        return weight;
    }

    public String getDescription() {
        return description;
    }
}
