package com.example.demo.base.base.entity;

import com.example.demo.base.risk.RiskSignal;
import jakarta.persistence.*;
@Entity
@Table(name = "risk_signal")
public class RiskSignalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "signal_code", nullable = false, length = 50)
    private RiskSignal signal;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "risk_assessment_id", nullable = false)
    private RiskAssessment riskAssessment;

    protected RiskSignalEntity() {}

    public RiskSignalEntity(RiskSignal signal, RiskAssessment ra) {
        this.signal = signal;
        this.riskAssessment = ra;
    }

    public RiskSignal getSignal() {
        return signal;
    }
}
