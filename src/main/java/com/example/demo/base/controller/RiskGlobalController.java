package com.example.demo.base.controller;

import com.example.demo.base.service.RiskAssessmentService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RiskGlobalController {

    private final RiskAssessmentService riskAssessmentService;

    public RiskGlobalController(RiskAssessmentService riskAssessmentService) {
        this.riskAssessmentService = riskAssessmentService;
    }

    @PostMapping("/clients/calculate-risk")
    public String calculateForAllClients() {

        riskAssessmentService.calculateForAllClients();

        return "redirect:/dashboard";
    }
}
