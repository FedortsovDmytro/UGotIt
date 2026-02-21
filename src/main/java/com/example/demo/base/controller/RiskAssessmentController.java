//package com.example.demo.base.controller;
//
//import com.example.demo.base.entity.Client;
//import com.example.demo.base.entity.CreditLimit;
//import com.example.demo.base.entity.ReceivableAging;
//import com.example.demo.base.entity.RiskAssessment;
//import com.example.demo.base.service.ClientService;
//import com.example.demo.base.service.CreditLimitService;
//import com.example.demo.base.service.ReceivableAgingService;
//import com.example.demo.base.service.RiskAssessmentService;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Optional;
//
//@Controller
//@RequestMapping("/clients/{externalId}/risk-assessments")
//public class RiskAssessmentController {
//
//    private final RiskAssessmentService riskAssessmentService;
//    private final ClientService clientService;
//private final ReceivableAgingService agingService;
//    private final CreditLimitService creditLimitService;
//    public RiskAssessmentController(
//            RiskAssessmentService riskAssessmentService,
//            ClientService clientService,
//            CreditLimitService creditLimitService,
//            ReceivableAgingService receivableAgingService
//    ) {
//        this.riskAssessmentService = riskAssessmentService;
//        this.clientService = clientService;
//        this.creditLimitService = creditLimitService;
//        this.agingService = receivableAgingService;
//    }
//
//    @PostMapping
//    public String calculateRisk(@PathVariable String externalId) {
//        var client = clientService.getByExternalId(externalId);
//        riskAssessmentService.assessAndSave(client);
//        return "redirect:/dashboard";
//    }
//
//    @PostMapping("/recalculate")
//    public String recalculateRisk(@PathVariable String externalId) {
//        var client = clientService.getByExternalId(externalId);
//        riskAssessmentService.recalculate(client);
//        return "redirect:/dashboard";
//    }
//    @GetMapping
//    public String showRiskDetails(
//            @PathVariable String externalId,
//            Model model
//    ) {
//        Client client = clientService.getByExternalId(externalId);
//        RiskAssessment assessment = riskAssessmentService
//                .getLatestForClient(client)
//                .orElse(null);
//
//
//        Optional<CreditLimit> creditLimit = creditLimitService.findByExternalId(client.getExternalId());
//        ReceivableAging aging = agingService.findByExternalId(client.getExternalId());
//
//
//        model.addAttribute("client", client);
//        model.addAttribute("risk", assessment);
//        model.addAttribute("creditLimit", creditLimit);
//        model.addAttribute("aging", aging);
//
//        System.out.println("CreditLimit object: " + creditLimit);
//        System.out.println("Aging object: " + aging);
//        if (creditLimit.isPresent()) {
//            System.out.println("limitAmount: " + creditLimit.get().getLimitAmount());
//            System.out.println("usedAmount: " + creditLimit.get().getUsedAmount());
//        }
//        if (aging != null) {
//            System.out.println("paymentTermsDays: " + aging.getPaymentTermsDays());
//        }
//
//        return "risk-details";
//    }
//
//}
//
package com.example.demo.base.controller;

import com.example.demo.base.entity.*;
import com.example.demo.base.service.ClientService;
import com.example.demo.base.service.CreditLimitService;
import com.example.demo.base.service.ReceivableAgingService;
import com.example.demo.base.service.RiskAssessmentService;
import com.example.demo.risk.RiskScoringEngine;
import com.example.demo.risk.RiskSignal;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/clients/{externalId}/risk-assessments")
public class RiskAssessmentController {

    private final RiskAssessmentService riskAssessmentService;
    private final ClientService clientService;
    private final ReceivableAgingService agingService;
    private final CreditLimitService creditLimitService;
    private final RiskScoringEngine riskScoringEngine;

    public RiskAssessmentController(
            RiskAssessmentService riskAssessmentService,
            ClientService clientService,
            CreditLimitService creditLimitService,
            ReceivableAgingService receivableAgingService,
            RiskScoringEngine riskScoringEngine
    ) {
        this.riskAssessmentService = riskAssessmentService;
        this.clientService = clientService;
        this.creditLimitService = creditLimitService;
        this.agingService = receivableAgingService;
        this.riskScoringEngine = riskScoringEngine;
    }

    @PostMapping
    public String calculateRisk(@PathVariable String externalId) {
        Client client = clientService.getByExternalId(externalId);
        riskAssessmentService.assessAndSave(client);
        return "redirect:/dashboard";
    }

    @PostMapping("/recalculate")
    public String recalculateRisk(@PathVariable String externalId) {
        Client client = clientService.getByExternalId(externalId);
        riskAssessmentService.recalculate(client);
        return "redirect:/dashboard";
    }

    @GetMapping
    public String showRiskDetails(@PathVariable String externalId, Model model) {
        Client client = clientService.getByExternalId(externalId);
        RiskAssessment assessment = riskAssessmentService
                .getLatestForClient(client)
                .orElse(null);

        Optional<CreditLimit> creditLimit = creditLimitService.findByExternalId(client.getExternalId());
        ReceivableAging aging = agingService.findByExternalId(client.getExternalId());

        Locale currentLocale = LocaleContextHolder.getLocale();

        if (assessment != null) {

            if (assessment.getSignals() != null && !assessment.getSignals().isEmpty()) {

                List<RiskSignal> signals = assessment.getSignals().stream()
                        .map(RiskSignalEntity::getSignal)
                        .toList();

                var result = riskScoringEngine.assess(signals, currentLocale);

                model.addAttribute("localizedReasons", result.getReasons());
                model.addAttribute("localizedRecommendation", result.getRecommendation());

            } else {

                model.addAttribute("localizedReasons", assessment.getReasons());
                model.addAttribute("localizedRecommendation", assessment.getRecommendation());
            }
        }

        System.out.println(client.getId() + " " + externalId);
        System.out.println(assessment);
        System.out.println(creditLimit);
        System.out.println(aging);
        model.addAttribute("client", client);
        model.addAttribute("risk", assessment);
        model.addAttribute("creditLimit", creditLimit);
        model.addAttribute("aging", aging);

        System.out.println("CreditLimit object: " + creditLimit);
        System.out.println("Aging object: " + aging);
        creditLimit.ifPresent(limit -> {
            System.out.println("limitAmount: " + limit.getLimitAmount());
            System.out.println("usedAmount: " + limit.getUsedAmount());
        });
        if (aging != null) {
            System.out.println("paymentTermsDays: " + aging.getPaymentTermsDays());
        }

        return "risk-details";
    }
}


