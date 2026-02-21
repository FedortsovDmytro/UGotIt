package com.example.demo.base.dto;

import com.example.demo.base.entity.Client;
import com.example.demo.risk.RiskLevel;

public class ClientDashboardRow {
    private Client client;
    private RiskLevel riskLevel;

    public ClientDashboardRow(Client client, RiskLevel riskLevel) {
        this.client = client;
        this.riskLevel = riskLevel;
    }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
}
