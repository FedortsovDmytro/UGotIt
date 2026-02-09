package com.example.demo.base.repository;

import com.example.demo.base.dto.ClientDashboardRow;
import com.example.demo.base.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientDashboardRepository extends JpaRepository<Client, Long> {

    @Query("""
    SELECT c, r
    FROM Client c
    LEFT JOIN RiskAssessment r 
        ON r.client = c 
        AND r.calculatedAt = (
            SELECT MAX(r2.calculatedAt)
            FROM RiskAssessment r2
            WHERE r2.client = c
        )
""")
    List<Object[]> fetchDashboardRaw();


}
