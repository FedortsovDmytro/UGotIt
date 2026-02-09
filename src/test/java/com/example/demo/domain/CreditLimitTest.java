package com.example.demo.domain;

import com.example.demo.base.entity.Client;
import com.example.demo.base.entity.ClientStatus;
import com.example.demo.base.entity.CreditLimit;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CreditLimitTest {

    @Test
    void shouldBuildCreditLimitWithAllFields() {
        Client client = new Client.Builder("ext1", "Client1", ClientStatus.ACTIVE).build();
        CreditLimit cl = new CreditLimit.Builder(client, BigDecimal.valueOf(10000), 30)
                .usedAmount(BigDecimal.valueOf(5000))
                .validFrom(LocalDate.now())
                .validTo(LocalDate.now().plusMonths(1))
                .build();

        assertEquals(client, cl.getClient());
        assertEquals(BigDecimal.valueOf(10000), cl.getLimitAmount());
        assertEquals(BigDecimal.valueOf(5000), cl.getUsedAmount());
        assertEquals(30, cl.getPaymentTermsDays());
        assertNotNull(cl.getValidFrom());
        assertNotNull(cl.getValidTo());
    }

    @Test
    void shouldThrowIfRequiredFieldsMissing() {
        assertThrows(IllegalStateException.class, () ->
                new CreditLimit.Builder(null, BigDecimal.valueOf(1000), 30)
                        .usedAmount(BigDecimal.valueOf(0))
                        .build()
        );

        Client client = new Client.Builder("ext1", "Client1", ClientStatus.ACTIVE).build();

        assertThrows(IllegalStateException.class, () ->
                new CreditLimit.Builder(client, null, 30)
                        .usedAmount(BigDecimal.valueOf(0))
                        .build()
        );

        assertThrows(IllegalStateException.class, () ->
                new CreditLimit.Builder(client, BigDecimal.valueOf(1000), -1)
                        .usedAmount(BigDecimal.valueOf(0))
                        .build()
        );
    }
}
