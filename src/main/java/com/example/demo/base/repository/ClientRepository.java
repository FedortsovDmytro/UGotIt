package com.example.demo.repository;

import com.example.demo.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByExternalId(String externalId);
    List<Client> findByFullNameContainingIgnoreCase(String name);
}

