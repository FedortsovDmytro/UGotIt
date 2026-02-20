package com.example.demo.base.base.repository;

import com.example.demo.base.base.entity.Client;
import com.example.demo.base.base.entity.CreditLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreditLimitExcelRepository extends JpaRepository<CreditLimit, Long> {
    Optional<CreditLimit> findByClient(Optional<Client> client);
}
