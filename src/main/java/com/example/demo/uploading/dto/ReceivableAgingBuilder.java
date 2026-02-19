package com.example.demo.uploading.dto;

import com.example.demo.risk.AgingResult;

import java.math.BigDecimal;
import java.util.List;

public class ReceivableAgingBuilder {

    public static AgingResult build(List<ReceivableRecord> records) {

        AgingResult result = new AgingResult();

        for (ReceivableRecord r : records) {

            if (r.getDaysPastDue() == null || r.getSaldo() == null)
                continue;

            BigDecimal amount = r.getSaldo();

            int days = r.getDaysPastDue();

            if (days <= 0)
                result.addCurrent(amount);
            else if (days <= 7)
                result.add1to7(amount);
            else if (days <= 14)
                result.add8to14(amount);
            else if (days <= 30)
                result.add15to30(amount);
            else if (days <= 60)
                result.add31to60(amount);
            else
                result.add60Plus(amount);
        }

        return result;
    }
}
