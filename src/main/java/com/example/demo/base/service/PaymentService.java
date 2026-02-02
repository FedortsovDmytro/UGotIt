package com.example.demo.base.service;

import com.example.demo.base.entity.Invoice;
import com.example.demo.base.entity.Payment;
import com.example.demo.base.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }
    public Payment createPayment(Invoice invoice, BigDecimal amount, LocalDate paymentDate, String method) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Payment amount must be positive");

        Payment payment = new Payment.Builder(invoice, paymentDate, amount)
                .method(method)
                .build();
        return paymentRepository.save(payment);
    }

    public List<Payment> getPaymentsForInvoice(Invoice invoice) {
        return paymentRepository.findByInvoice(invoice);
    }
}
