package com.example.demo.base.service;

import com.example.demo.base.entity.Client;
import com.example.demo.base.entity.CreditLimit;
import com.example.demo.base.repository.ClientRepository;
import com.example.demo.base.repository.CreditLimitExcelRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class CreditLimitService {

    private final CreditLimitExcelRepository creditLimitRepository;
    private final ClientRepository clientRepository;

    public CreditLimitService(
            CreditLimitExcelRepository creditLimitRepository,
            ClientRepository clientRepository
    ) {
        this.creditLimitRepository = creditLimitRepository;
        this.clientRepository = clientRepository;
    }

    public CreditLimit createCreditLimit(
            String externalId,
            BigDecimal limitAmount,
            int paymentTermsDays
    ) {
        Client client = clientRepository.findByExternalId(externalId)
                .orElseThrow(() -> new IllegalStateException("Client not found"));

        CreditLimit cl = new CreditLimit.Builder(client, limitAmount, paymentTermsDays)
                .usedAmount(BigDecimal.ZERO)
                .validFrom(LocalDate.now())
                .validTo(LocalDate.now().plusMonths(1))
                .build();

        return creditLimitRepository.save(cl);
    }

    public boolean canPlaceOrder(String externalId, BigDecimal amount) {
        Client client = clientRepository.findByExternalId(externalId)
                .orElseThrow();

        CreditLimit cl = creditLimitRepository.findByClient(Optional.of(client))
                .orElseThrow();

        return cl.canCover(amount);
    }

    public void updateUsedAmount(String externalId, BigDecimal newUsedAmount) {
        Client client = clientRepository.findByExternalId(externalId)
                .orElseThrow();

        CreditLimit cl = creditLimitRepository.findByClient(Optional.of(client))
                .orElseThrow();

        cl.setUsedAmount(newUsedAmount);
        creditLimitRepository.save(cl);
    }

    public CreditLimit getCreditLimit(String externalId) {
        Client client = clientRepository.findByExternalId(externalId)
                .orElseThrow();
        CreditLimit cl = creditLimitRepository.findByClient(Optional.of(client))
                .orElseThrow();
        return cl;
    }

    public CreditLimit findByClient(Client client) {
        return creditLimitRepository.findByClient(Optional.ofNullable(client)).orElse(null);
    }

    public Optional<CreditLimit> findByExternalId(String externalId) {
        return creditLimitRepository.findByClient(clientRepository.findByExternalId(externalId));
    }
}
