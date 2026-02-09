package com.example.demo.base.service;

import com.example.demo.base.entity.*;
import com.example.demo.base.repository.RiskAssessmentRepository;
import com.example.demo.risk.RiskAssessmentMapper;
import com.example.demo.risk.RiskAssessmentResult;
import com.example.demo.risk.RiskLevel;
import com.example.demo.risk.RiskScoreCalculator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RiskAssessmentService {

    private final RiskScoreCalculator calculator;
    private final RiskAssessmentRepository repository;
    private final CreditLimitService creditLimitService;
    private final ReceivableAgingService agingService;
    private final BisnodeService bisnodeService;

    public RiskAssessmentService(RiskAssessmentRepository repository,CreditLimitService creditLimitService,ReceivableAgingService receivableAgingService, BisnodeService bisnodeService) {

        this.repository = repository;
        this.bisnodeService = bisnodeService;
        this.calculator = new RiskScoreCalculator();
        this.creditLimitService = creditLimitService;
        this.agingService = receivableAgingService;

    }

    public RiskAssessment assessAndSave(Client client) {

        CreditLimit creditLimit =
                creditLimitService.findByClient(client);

        ReceivableAging aging =
                agingService.findCurrentByClient(client);

        Bisnode bisnode =
                bisnodeService.findLatestByClient(client);

        RiskAssessmentResult result =
                calculator.assess(client, creditLimit, aging, bisnode);

        RiskAssessment assessment =
                RiskAssessmentMapper.toEntity(client, result);

        return repository.save(assessment);
    }
       public RiskAssessment calculateRisk(Client client, int score, String reasons, String recommendation) {
        RiskLevel level;
        if (score >= 80) level = RiskLevel.HIGH;
        else if (score >= 50) level = RiskLevel.MEDIUM;
        else level = RiskLevel.LOW;

        RiskAssessment ra = new RiskAssessment.Builder(client, score, level, LocalDateTime.now())
                .reasons(reasons)
                .recommendation(recommendation)
                .build();

        return repository.save(ra);
    }

    public List<RiskAssessment> getForClient(Client client) {
        return repository.findByClient(client);
    }

    public Optional<RiskAssessment> getLatestForClient(Client client) {
        return repository
                .findTopByClientOrderByCalculatedAtDesc(client);
    }

    @Transactional
    public void recalculate(Client client) {
        repository.deleteByClient(client);
        assessAndSave(client);
    }

}
