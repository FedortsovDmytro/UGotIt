package com.example.demo.base.excelUpload.repository;

import com.example.demo.base.base.entity.Bisnode;
import com.example.demo.base.base.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BisnodeRepository extends JpaRepository<Bisnode, Long> {
    Optional<Bisnode> findByExternalId(Long externalId);
    Optional<Bisnode> findTopByClientOrderByFetchedAtDesc(Client client);
}