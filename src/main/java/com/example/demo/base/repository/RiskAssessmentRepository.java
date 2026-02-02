package com.example.demo.base.repository;

import com.example.demo.base.entity.Client;
import com.example.demo.base.entity.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {
    List<RiskAssessment> findByClient(Client client);

    List<RiskAssessment> findTop10ByClientIdOrderByCalculatedAtDesc(Long clientId);
}
