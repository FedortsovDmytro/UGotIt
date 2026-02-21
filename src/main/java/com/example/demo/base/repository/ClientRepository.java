package com.example.demo.base.repository;

import com.example.demo.base.entity.Client;
import com.example.demo.base.entity.ClientStatus;
import com.example.demo.base.entity.CreditLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByExternalId(String externalId);
    List<Client> findByFullNameContainingIgnoreCase(String name);

    long countByStatus(ClientStatus status);
//    @Query("SELECT cl FROM CreditLimit cl WHERE cl.client = :client")
//    Optional<CreditLimit> getCreditLimit(@Param("client") Client client);
//     Optional<CreditLimit> findCreditLimit(Client client);

    //CreditLimit saveCreditLimit(CreditLimit limit);
}

