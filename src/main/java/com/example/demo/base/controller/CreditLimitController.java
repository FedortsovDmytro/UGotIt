package com.example.demo.base.controller;

import com.example.demo.base.dto.CreateCreditLimitRequest;
import com.example.demo.base.entity.CreditLimit;
import com.example.demo.base.service.ClientService;
import com.example.demo.base.service.CreditLimitService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clients/{externalId}/credit-limit")
public class CreditLimitController {

    private final CreditLimitService creditLimitService;
 private final ClientService clientService;

    public CreditLimitController(CreditLimitService creditLimitService, ClientService clientService) {
        this.creditLimitService = creditLimitService;
        this.clientService = clientService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreditLimit createCreditLimit(
            @PathVariable String externalId,
            @RequestBody CreateCreditLimitRequest request
    ) {
        return creditLimitService.createCreditLimit(
                externalId,
                request.limitAmount(),
                request.paymentTermsDays()
        );
    }

    @GetMapping
    public CreditLimit getCreditLimit(@PathVariable String externalId) {
        return creditLimitService.getCreditLimit(externalId);
    }
}
