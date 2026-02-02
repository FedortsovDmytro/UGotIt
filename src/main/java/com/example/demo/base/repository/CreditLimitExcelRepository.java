package com.example.demo.repository;

import com.example.demo.entity.Client;
import com.example.demo.entity.CreditLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreditLimitExcelRepository extends JpaRepository<CreditLimit, Long> {
    Optional<CreditLimit> findByClient(Client client);
}
