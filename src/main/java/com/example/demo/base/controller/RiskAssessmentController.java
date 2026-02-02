package com.example.demo.base.controller;

import com.example.demo.base.dto.RiskAssessmentRequest;
import com.example.demo.base.entity.Client;
import com.example.demo.base.entity.RiskAssessment;
import com.example.demo.base.service.ClientService;
import com.example.demo.base.service.RiskAssessmentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    public RiskAssessment calculateRisk(@PathVariable String externalId) {

        Client client = clientService.getByExternalId(externalId);

        return riskAssessmentService.assessAndSave(client);
    }
}
