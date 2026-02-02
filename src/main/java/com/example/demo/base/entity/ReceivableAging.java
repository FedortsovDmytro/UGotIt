package com.example.demo.base.entity;

import jakarta.persistence.*;
import jakarta.persistence.Id;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "receivable_aging")
public class ReceivableAging {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Client client;

    @Column(nullable = false)
    private LocalDate reportDate;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private BigDecimal notDue;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }
    private BigDecimal overdue1to7;
    private BigDecimal overdue8to14;
    private BigDecimal overdue15to30;
    private BigDecimal overdue31to60;
    private BigDecimal overdue61to90;
    private BigDecimal overdue91to120;
    private BigDecimal overdue121to360;
    private BigDecimal overdueAbove360;

    private Integer paymentTermsDays;
    private String legalEvents;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getNotDue() {
        return notDue;
    }

    public void setNotDue(BigDecimal notDue) {
        this.notDue = notDue;
    }

    public BigDecimal getOverdue1to7() {
        return overdue1to7;
    }

    public void setOverdue1to7(BigDecimal overdue1to7) {
        this.overdue1to7 = overdue1to7;
    }

    public BigDecimal getOverdue8to14() {
        return overdue8to14;
    }

    public void setOverdue8to14(BigDecimal overdue8to14) {
        this.overdue8to14 = overdue8to14;
    }

    public BigDecimal getOverdue15to30() {
        return overdue15to30;
    }

    public void setOverdue15to30(BigDecimal overdue15to30) {
        this.overdue15to30 = overdue15to30;
    }

    public BigDecimal getOverdue31to60() {
        return overdue31to60;
    }

    public void setOverdue31to60(BigDecimal overdue31to60) {
        this.overdue31to60 = overdue31to60;
    }

    public BigDecimal getOverdue61to90() {
        return overdue61to90;
    }

    public void setOverdue61to90(BigDecimal overdue61to90) {
        this.overdue61to90 = overdue61to90;
    }

    public BigDecimal getOverdue91to120() {
        return overdue91to120;
    }

    public void setOverdue91to120(BigDecimal overdue91to120) {
        this.overdue91to120 = overdue91to120;
    }

    public BigDecimal getOverdue121to360() {
        return overdue121to360;
    }

    public void setOverdue121to360(BigDecimal overdue121to360) {
        this.overdue121to360 = overdue121to360;
    }

    public BigDecimal getOverdueAbove360() {
        return overdueAbove360;
    }

    public void setOverdueAbove360(BigDecimal overdueAbove360) {
        this.overdueAbove360 = overdueAbove360;
    }

    public Integer getPaymentTermsDays() {
        return paymentTermsDays;
    }

    public void setPaymentTermsDays(Integer paymentTermsDays) {
        this.paymentTermsDays = paymentTermsDays;
    }

    public String getLegalEvents() {
        return legalEvents;
    }

    public void setLegalEvents(String legalEvents) {
        this.legalEvents = legalEvents;
    }

    public ReceivableAging() {}
}
