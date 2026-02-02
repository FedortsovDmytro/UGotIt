package com.example.demo.base.repository;


import com.example.demo.base.entity.RiskSignalEntity;
import com.example.demo.risk.RiskSignal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskSignalRepository
        extends JpaRepository<RiskSignalEntity, Long> {

    List<RiskSignalEntity> findBySignal(RiskSignal signal);

    List<RiskSignalEntity> findByRiskAssessmentId(Long assessmentId);
}
