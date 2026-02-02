package com.example.demo.repository;

import com.example.demo.entity.Client;
import com.example.demo.entity.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {
    List<RiskAssessment> findByClient(Client client);
}
