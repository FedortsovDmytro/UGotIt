
        package com.example.demo.base.base.controller;

import com.example.demo.base.base.entity.ClientStatus;
import com.example.demo.base.base.repository.RiskAssessmentRepository;
import com.example.demo.base.base.service.ClientService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

        @Controller
public class DashboardController {

    private final ClientService clientService;
    private final RiskAssessmentRepository riskRepository;

    public DashboardController(ClientService clientService,
                               RiskAssessmentRepository riskRepository) {
        this.clientService = clientService;
        this.riskRepository = riskRepository;
    }


    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model
    ) {

        Sort.Direction direction =
                dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        Sort sortSpec = Sort.by(direction, sort);

        // Get FULL sorted dashboard list
        var allItems = clientService.getDashboardItems(sortSpec);

        // ----- Manual pagination -----
        int start = page * size;
        int end = Math.min(start + size, allItems.size());

        var pageItems = start > allItems.size()
                ? List.of()
                : allItems.subList(start, end);

        int totalPages = (int) Math.ceil((double) allItems.size() / size);

        // ----- Model -----
        model.addAttribute("items", pageItems);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", size);

        model.addAttribute("totalItems", allItems.size());

        model.addAttribute("activeItems", allItems.stream()
                .filter(i -> i.getClient().getStatus() == ClientStatus.ACTIVE)
                .count());

        model.addAttribute("blockedItems", allItems.stream()
                .filter(i -> i.getClient().getStatus() == ClientStatus.BLOCKED)
                .count());

        model.addAttribute("username", "Admin");

        return "dashboard";
    }
    @GetMapping("/import-clients")
    public String showImportClientsPage() {
        return "upload-files";
    }
}