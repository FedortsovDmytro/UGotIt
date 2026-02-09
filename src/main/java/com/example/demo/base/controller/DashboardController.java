package com.example.demo.base.controller;

import com.example.demo.base.dto.ClientDashboardView;
import com.example.demo.base.entity.ClientStatus;
import com.example.demo.base.entity.RiskAssessment;
import com.example.demo.base.repository.ClientRepository;
import com.example.demo.base.repository.RiskAssessmentRepository;
import com.example.demo.base.service.ClientService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static com.example.demo.base.entity.ClientStatus.ACTIVE;
@Controller
public class DashboardController {

    private final ClientService clientService;
    private final RiskAssessmentRepository riskRepository;

    public DashboardController(ClientService clientService,
                               RiskAssessmentRepository riskRepository) {
        this.clientService = clientService;
        this.riskRepository = riskRepository;
    }
//
//    @GetMapping("/dashboard")
//    public String dashboard(Model model) {
//
//        var clients = clientRepository.findAll();
//
//        var dashboardItems = clients.stream()
//                .map(client -> {
//                    var risk = riskRepository
//                            .findTopByClientOrderByCalculatedAtDesc(client)
//                            .map(RiskAssessment::getRiskLevel)
//                            .orElse(null);
//
//                    return new ClientDashboardView(client, risk);
//                })
//                .toList();
//
//        model.addAttribute("items", dashboardItems);
//
//        model.addAttribute("totalItems", clients.size());
//        model.addAttribute("activeItems",
//                clientRepository.countByStatus(ClientStatus.ACTIVE));
//        model.addAttribute("blockedItems",
//                clientRepository.countByStatus(ClientStatus.BLOCKED));
//
//        model.addAttribute("username", "Admin");
//
//        return "dashboard";
//    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model
    ) {
        Sort.Direction direction =
                dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        Sort sortSpec = switch (sort) {
            case "name" -> Sort.by(direction, "name");
            case "risk" -> Sort.by(direction, "risk");
            default -> Sort.by(direction, "id");
        };

        var items = clientService.getDashboardItems(sortSpec);

        // Add attributes for Thymeleaf template
        model.addAttribute("items", items);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("totalItems", items.size());
        model.addAttribute("activeItems", items.stream()
                .filter(i -> i.getClient().getStatus() == ClientStatus.ACTIVE)
                .count());
        model.addAttribute("blockedItems", items.stream()
                .filter(i -> i.getClient().getStatus() == ClientStatus.BLOCKED)
                .count());
        model.addAttribute("username", "Admin");

        return "dashboard";
    }

}
