package com.example.demo.service;

import com.example.demo.entity.Client;
import com.example.demo.entity.RiskAssessment;
import com.example.demo.entity.RiskLevel;
import com.example.demo.repository.RiskAssessmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RiskAssessmentService {

    private final RiskAssessmentRepository riskAssessmentRepository;

    RiskAssessmentService(RiskAssessmentRepository riskAssessmentRepository) {
        this.riskAssessmentRepository = riskAssessmentRepository;
    }
    public RiskAssessment calculateRisk(Client client, int score, String reasons, String recommendation) {
        RiskLevel level;
        if (score >= 80) level = RiskLevel.HIGH;
        else if (score >= 50) level = RiskLevel.MEDIUM;
        else level = RiskLevel.LOW;

        RiskAssessment ra = new RiskAssessment.Builder(client, score, level)
                .reasons(reasons)
                .recommendation(recommendation)
                .build();

        return riskAssessmentRepository.save(ra);
    }

    public List<RiskAssessment> getForClient(Client client) {
        return riskAssessmentRepository.findByClient(client);
    }
}
