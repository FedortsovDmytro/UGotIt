package com.example.demo.base.base.dto;

import com.example.demo.base.base.entity.Client;
import com.example.demo.base.risk.RiskLevel;

public class ClientDashboardView {

    private final Client client;
    private final RiskLevel riskLevel;

    public ClientDashboardView(Client client, RiskLevel riskLevel) {
        this.client = client;
        this.riskLevel = riskLevel;
    }

    public Client getClient() {
        return client;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }
}
