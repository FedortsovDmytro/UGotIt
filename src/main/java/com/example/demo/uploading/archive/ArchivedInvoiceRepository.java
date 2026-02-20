package com.example.demo.base.uploading.archive;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArchivedInvoiceRepository
        extends JpaRepository<ArchivedInvoice, Long> {

    List<ArchivedInvoice> findByClientExternalId(String clientExternalId);
}
