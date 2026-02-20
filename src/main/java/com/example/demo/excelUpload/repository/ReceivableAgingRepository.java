package com.example.demo.base.excelUpload.repository;

import com.example.demo.base.base.entity.Client;
import com.example.demo.base.base.entity.ReceivableAging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReceivableAgingRepository
        extends JpaRepository<ReceivableAging, Long> {

    Optional<ReceivableAging> findTopByClientOrderByCalculatedAtDesc(Client client);

    boolean existsCurrentByClient(Client client);

    ReceivableAging findByClientExternalId(String externalId);

    void deleteByClient(Client client);
}
