package com.example.demo.base.base.repository;

import com.example.demo.base.base.entity.Client;
import com.example.demo.base.base.entity.ClientStatus;
import org.springframework.data.jpa.repository.JpaRepository;

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

