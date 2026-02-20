package com.example.demo.base.base.repository;

import com.example.demo.base.base.entity.Invoice;
import com.example.demo.base.base.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByInvoice(Invoice invoice);
}
