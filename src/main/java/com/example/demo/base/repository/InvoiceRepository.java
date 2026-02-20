package com.example.demo.base.base.repository;

import com.example.demo.base.base.entity.Client;
import com.example.demo.base.base.entity.Invoice;
import com.example.demo.base.base.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByClient(Client client);

    List<Invoice> findByClientAndStatus(Client client, InvoiceStatus status);
}
