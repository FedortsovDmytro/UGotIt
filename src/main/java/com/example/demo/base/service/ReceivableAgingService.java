package com.example.demo.base.service;

import com.example.demo.base.entity.Client;
import com.example.demo.base.entity.Invoice;
import com.example.demo.base.entity.ReceivableAging;
import com.example.demo.excelUpload.repository.ReceivableAgingRepository;
import com.example.demo.risk.AgingResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReceivableAgingService {

    private final ReceivableAgingRepository repository;

    public ReceivableAgingService(ReceivableAgingRepository repository) {
        this.repository = repository;
    }
    public ReceivableAging findCurrentByClient(Client client) {
        return repository
                .findTopByClientOrderByCalculatedAtDesc(client)
                .orElse(null);
    }
    public AgingResult calculate(List<Invoice> invoices) {
        AgingResult result = new AgingResult();
        LocalDate today = LocalDate.now();

        for (Invoice invoice : invoices) {
            if (invoice.getDueDate() == null) continue;

            long days = ChronoUnit.DAYS.between(invoice.getDueDate(), today);
            BigDecimal amount = invoice.getAmount();

            if (days <= 0) result.addCurrent(amount);
            else if (days <= 7) result.add1to7(amount);
            else if (days <= 14) result.add8to14(amount);
            else if (days <= 30) result.add15to30(amount);
            else if (days <= 60) result.add31to60(amount);
            else result.add60Plus(amount);
        }

        return result;
    }

    public ReceivableAging findByExternalId(String externalId) {
        return repository.findByClientExternalId(externalId);
    }
}
