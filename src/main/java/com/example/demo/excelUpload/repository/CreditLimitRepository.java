package com.example.demo.excelUpload.repository;

import com.example.demo.base.entity.Client;
import com.example.demo.base.entity.CreditLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreditLimitRepository extends JpaRepository<CreditLimit, Long> {
    Optional<CreditLimit> findByClient(Client client);
}
