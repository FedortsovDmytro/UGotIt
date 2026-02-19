package com.example.demo.uploading.archive;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArchivedClientRepository extends JpaRepository<ArchivedClient, Long> {

    List<ArchivedClient> findByExternalId(String externalId);
//    Optional<Client> existing = clientRepository.findByExternalId(externalId);
//
//if (existing.isPresent()) {
//        archivedClientRepository.save(
//                ArchivedClient.from(existing.get(), "REPLACED_BY_UPLOAD")
//        );
//        clientRepository.delete(existing.get());
//    }

}
