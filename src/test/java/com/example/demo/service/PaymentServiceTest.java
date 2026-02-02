package com.example.demo.service;

import com.example.demo.base.entity.Invoice;
import com.example.demo.base.entity.Payment;
import com.example.demo.base.repository.PaymentRepository;
import com.example.demo.base.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    private PaymentRepository paymentRepository;
    private PaymentService paymentService;
    private Invoice invoice = Mockito.mock(Invoice.class);

    @BeforeEach
    void setUp() {
        paymentRepository = Mockito.mock(PaymentRepository.class);
        paymentService = new PaymentService(paymentRepository);
    }

    @Test
    void createPayment_shouldSavePayment() {
        Payment payment = new Payment.Builder(invoice, LocalDate.now(), BigDecimal.valueOf(500))
                .method("CARD")
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        Payment result = paymentService.createPayment(invoice, BigDecimal.valueOf(500), LocalDate.now(), "CARD");

        assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(500));
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void getPaymentsForInvoice_shouldReturnList() {
        Payment payment = new Payment.Builder(invoice, LocalDate.now(), BigDecimal.valueOf(500)).build();
        when(paymentRepository.findByInvoice(invoice)).thenReturn(List.of(payment));

        List<Payment> result = paymentService.getPaymentsForInvoice(invoice);

        assertThat(result).hasSize(1);
        verify(paymentRepository, times(1)).findByInvoice(invoice);
    }
}
