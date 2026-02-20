package com.example.demo.base.base.repository;

import com.example.demo.base.base.entity.Client;
import com.example.demo.base.base.entity.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {
    List<RiskAssessment> findByClient(Client client);

    List<RiskAssessment> findTop10ByClientIdOrderByCalculatedAtDesc(Long clientId);

    Optional<RiskAssessment> findTopByClientOrderByCalculatedAtDesc(Client client);
    @Query("SELECT r FROM RiskAssessment r LEFT JOIN FETCH r.signals WHERE r.client = :client ORDER BY r.calculatedAt DESC")
    Optional<RiskAssessment> findTopByClientWithSignals(@Param("client") Client client);
    void deleteByClient(Client client);
    List<RiskAssessment> findAllByClientOrderByCalculatedAtDesc(Client client);
}
