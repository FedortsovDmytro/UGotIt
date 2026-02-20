package com.example.demo.base.uploading.service;

import com.example.demo.base.base.entity.Client;
import com.example.demo.base.base.entity.RiskAssessment;
import com.example.demo.base.base.repository.RiskAssessmentRepository;
import com.example.demo.base.risk.AgingResult;
import com.example.demo.base.risk.RiskAssessmentMapper;
import com.example.demo.base.risk.RiskAssessmentResult;
import com.example.demo.base.risk.RiskScoreCalculator;
import com.example.demo.base.uploading.dto.ReceivableAgingBuilder;
import com.example.demo.base.uploading.dto.ReceivableRecord;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RiskProcessingService {

    private final RiskScoreCalculator calculator;
    private final RiskAssessmentRepository repository;

    public RiskProcessingService(
            RiskScoreCalculator calculator,
            RiskAssessmentRepository repository
    ) {
        this.calculator = calculator;
        this.repository = repository;
    }

    public void recalculate(Client client, List<ReceivableRecord> records) {

        AgingResult aging = ReceivableAgingBuilder.build(records);

        RiskAssessmentResult result =
                calculator.assess(client, null, null, null);

        RiskAssessment entity =
                RiskAssessmentMapper.toEntity(client, result);

        repository.save(entity);
    }
}
