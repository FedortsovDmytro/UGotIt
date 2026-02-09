package com.example.demo.integration;

import com.example.demo.base.entity.*;
import com.example.demo.base.service.*;
import com.example.demo.risk.RiskLevel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class InvoiceServicesIntegrationTest {

    @Autowired
    private ClientService clientService;

    @Autowired
    private CreditLimitService creditLimitService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RiskAssessmentService riskAssessmentService;

    @Test
    void shouldHandleFullBusinessFlowCorrectly() {
        Client client = clientService.createClient(
                "ext-100",
                "Bob Builder",
                ClientStatus.ACTIVE
        );

        CreditLimit creditLimit = creditLimitService.createCreditLimit(
                client.getExternalId(),
                BigDecimal.valueOf(1000),
                30
        );

        assertThat(client.getId()).isNotNull();
        assertThat(creditLimit.getId()).isNotNull();
        assertThat(creditLimit.canCover(BigDecimal.valueOf(500))).isTrue();

        Invoice invoice = invoiceService.createInvoice(
                client.getId(),
                "INV-100",
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                BigDecimal.valueOf(200),
                "USD",
                InvoiceStatus.OPEN
        );

        assertThat(invoice.getId()).isNotNull();
        assertThat(invoice.getClient()).isEqualTo(client);

        List<Invoice> openInvoices = invoiceService.getOpenInvoices(client);
        assertThat(openInvoices)
                .hasSize(1)
                .first()
                .extracting(Invoice::getInvoiceNumber)
                .isEqualTo("INV-100");

        Payment payment = paymentService.createPayment(
                invoice,
                BigDecimal.valueOf(100),
                LocalDate.now(),
                "BANK_TRANSFER"
        );

        assertThat(payment.getId()).isNotNull();
        assertThat(payment.getInvoice()).isEqualTo(invoice);

        List<Payment> payments = paymentService.getPaymentsForInvoice(invoice);
        assertThat(payments).hasSize(1);

        creditLimitService.updateUsedAmount(client.getExternalId(), BigDecimal.valueOf(100));

        assertThat(creditLimitService.canPlaceOrder(client.getExternalId(), BigDecimal.valueOf(950)))
                .isFalse();

        RiskAssessment riskAssessment = riskAssessmentService.calculateRisk(
                client,
                75,
                "Late payments risk",
                "Shorten payment terms"
        );

        assertThat(riskAssessment.getId()).isNotNull();
        assertThat(riskAssessment.getClient()).isEqualTo(client);
        assertThat(riskAssessment.getRiskLevel()).isEqualTo(RiskLevel.MEDIUM);
    }
}
