package com.example.demo.uploading.service;

import com.example.demo.base.entity.Client;
import com.example.demo.uploading.dto.ReceivableRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class RiskCalculationService {

    private final RiskProcessingService riskProcessingService;

    public RiskCalculationService(RiskProcessingService riskProcessingService) {
        this.riskProcessingService = riskProcessingService;
    }

    public void calculateForAll(Map<Client, List<ReceivableRecord>> data) {

        for (var entry : data.entrySet()) {
            riskProcessingService.recalculate(
                    entry.getKey(),
                    entry.getValue()
            );
        }
    }
}
