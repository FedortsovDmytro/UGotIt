package com.example.demo.base.service;

import com.example.demo.base.entity.Client;
import com.example.demo.base.entity.CreditLimit;
import com.example.demo.base.entity.ReceivableAging;
import com.example.demo.base.repository.ClientRepository;
import com.example.demo.excelUpload.repository.CreditLimitRepository;
import com.example.demo.excelUpload.repository.ReceivableAgingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Transactional
public class ClientBootstrapService {

    private final ClientRepository clientRepository;
    private final CreditLimitRepository creditLimitRepository;
    private final ReceivableAgingRepository agingRepository;

    public ClientBootstrapService(ClientRepository clientRepository,
                                  CreditLimitRepository creditLimitRepository,
                                  ReceivableAgingRepository agingRepository) {
        this.clientRepository = clientRepository;
        this.creditLimitRepository = creditLimitRepository;
        this.agingRepository = agingRepository;
    }

    public void createClientIfNotExists(Client client) {
        clientRepository.save(client);

        if (creditLimitRepository.findByClient(client)==null) {
            CreditLimit creditLimit = new CreditLimit.Builder(client, BigDecimal.ZERO, 30)
                    .usedAmount(BigDecimal.ZERO)
                    .validFrom(LocalDate.now())
                    .build();
            creditLimitRepository.save(creditLimit);
        }

        if (!agingRepository.existsCurrentByClient(client)) {
            ReceivableAging aging = new ReceivableAging();
            aging.setClient(client);
            aging.setReportDate(LocalDate.now());
            aging.setTotalAmount(BigDecimal.ZERO);
            aging.setNotDue(BigDecimal.ZERO);
            agingRepository.save(aging);
        }
    }
}
