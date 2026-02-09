package com.example.demo.base.controller;

import com.example.demo.base.entity.Client;
import com.example.demo.base.entity.RiskAssessment;
import com.example.demo.base.service.ClientService;
import com.example.demo.base.service.RiskAssessmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
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
    public String calculateRisk(@PathVariable String externalId) {
        var client = clientService.getByExternalId(externalId);
        riskAssessmentService.assessAndSave(client);
        return "redirect:/dashboard";
    }

    @PostMapping("/recalculate")
    public String recalculateRisk(@PathVariable String externalId) {
        var client = clientService.getByExternalId(externalId);
        riskAssessmentService.recalculate(client);
        return "redirect:/dashboard";
    }
    @GetMapping
    public String showRiskDetails(
            @PathVariable String externalId,
            Model model
    ) {
        Client client = clientService.getByExternalId(externalId);

        RiskAssessment assessment = riskAssessmentService
                .getLatestForClient(client)
                .orElseThrow(() -> new IllegalStateException("Risk not calculated"));

        model.addAttribute("client", client);
        model.addAttribute("risk", assessment);

        return "risk-details";
    }
}

