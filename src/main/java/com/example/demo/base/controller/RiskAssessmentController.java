package com.example.demo.controller;

import com.example.demo.dto.RiskAssessmentRequest;
import com.example.demo.entity.Client;
import com.example.demo.entity.RiskAssessment;
import com.example.demo.service.ClientService;
import com.example.demo.service.PaymentService;
import com.example.demo.service.RiskAssessmentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/clients/{externalId}/risk-assessments")
public class RiskAssessmentController {

    private final RiskAssessmentService riskAssessmentService;
    private final ClientService clientService;

    public RiskAssessmentController(
            RiskAssessmentService riskAssessmentService,
            ClientService clientService
    ) {
        this.riskAssessmentService = riskAssessmentService;
        this.clientService = clientService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RiskAssessment calculateRisk(
            @PathVariable String externalId,
            @RequestBody RiskAssessmentRequest request
    ) {
        Client client = clientService.getByExternalId(externalId);

        return riskAssessmentService.calculateRisk(
                client,
                request.score(),
                request.reasons(),
                request.recommendation()
        );
    }
}
