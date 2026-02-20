package com.example.demo.base.uploading.archive;

import com.example.demo.base.base.entity.Client;
import com.example.demo.base.base.entity.ClientStatus;
import com.example.demo.base.base.entity.Invoice;
import com.example.demo.base.base.repository.ClientRepository;
import com.example.demo.base.base.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class ArchiveService {

    private final ClientRepository clientRepository;
    private final ArchivedClientRepository archivedClientRepository;
    private final InvoiceRepository invoiceRepository;
    private final ArchivedInvoiceRepository archivedInvoiceRepository;

    public ArchiveService(
            ArchivedInvoiceRepository archivedInvoiceRepository,
            InvoiceRepository invoiceRepository,
            ClientRepository clientRepository,
            ArchivedClientRepository archivedClientRepository
    ) {
        this.archivedInvoiceRepository = archivedInvoiceRepository;
        this.invoiceRepository = invoiceRepository;
        this.clientRepository = clientRepository;
        this.archivedClientRepository = archivedClientRepository;
    }

    @Transactional
    public void archiveClientWithInvoices(Client client, ArchiveReason reason) {

        var invoices = invoiceRepository.findByClient(client);
        for (Invoice invoice : invoices) {
            ArchivedInvoice archived = ArchivedInvoice.fromInvoice(invoice);
            archivedInvoiceRepository.save(archived);
        }
        invoiceRepository.deleteAll(invoices);


        client.setStatus(ClientStatus.ARCHIVED);
        archivedClientRepository.save(ArchivedClient.from(client, reason.name()));

        clientRepository.save(client);
    }

}
