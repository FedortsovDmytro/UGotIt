package com.example.demo.uploading;

import com.example.demo.base.entity.Client;
import com.example.demo.base.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ArchiveService {

    private final ClientRepository clientRepository;
    private final ArchivedClientRepository archivedClientRepository;

    public ArchiveService(
            ClientRepository clientRepository,
            ArchivedClientRepository archivedClientRepository
    ) {
        this.clientRepository = clientRepository;
        this.archivedClientRepository = archivedClientRepository;
    }
    
    @Transactional
    public void archiveAndRemove(Client client, String reason) {
        ArchivedClient archivedClient = ArchivedClient.from(client, reason);

        archivedClientRepository.save(archivedClient);
        clientRepository.delete(client);
    }


    @Transactional
    public boolean archiveIfExists(String externalId, String reason) {
        return clientRepository.findByExternalId(externalId)
                .map(client -> {
                    archiveAndRemove(client, reason);
                    return true;
                })
                .orElse(false);
    }

    public void archive(Client client, String replacedByExcelUpload) {
    }
}
