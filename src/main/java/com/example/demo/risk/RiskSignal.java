package com.example.demo.risk;

public enum RiskSignal {

    OVERDUE_1_7(5, "Opóźnienia płatności 1–7 dni"),
    OVERDUE_8_14(10, "Opóźnienia płatności 8–14 dni"),
    OVERDUE_15_30(20, "Opóźnienia płatności 15–30 dni"),
    OVERDUE_31_60(45, "Opóźnienia płatności 31–60 dni"),
    OVERDUE_60_PLUS(80, "Opóźnienia płatności powyżej 60 dni"),

    DSO_TREND_UP_5(5, "Wzrost DSO o 5 dni"),
    DSO_TREND_UP_10(10, "Wzrost DSO o 10 dni"),
    DSO_TREND_UP_15(20, "Wzrost DSO o 15 dni"),


    LIMIT_USAGE_70(5, "Wykorzystanie limitu powyżej 70%"),
    LIMIT_USAGE_85(15, "Wykorzystanie limitu powyżej 85%"),
    LIMIT_USAGE_95(35, "Wykorzystanie limitu powyżej 95%"),

    EXTERNAL_RATING_C(15, "Rating zewnętrzny C"),
    EXTERNAL_RATING_D(35, "Rating zewnętrzny D"),
    NO_EXTERNAL_RATING(10, "Brak zewnętrznego ratingu"),


    NEW_CLIENT(10, "Nowy klient bez historii płatniczej"),


    NO_CREDIT_LIMIT_LOW_EXPOSURE(
            10,
            "Brak limitu kredytowego przy niskiej ekspozycji"
    ),
    NO_CREDIT_LIMIT_MEDIUM_EXPOSURE(
            30,
            "Brak limitu kredytowego przy średniej ekspozycji"
    ),
    NO_CREDIT_LIMIT_HIGH_EXPOSURE(
            60,
            "Brak limitu kredytowego przy wysokiej ekspozycji"
    ),


    NO_AGING_DATA(40, "Brak danych aging należności");

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
