package com.example.demo.ekselUploud.repository;

import com.example.demo.entity.Client;
import com.example.demo.entity.CreditLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface CreditLimitRepository extends JpaRepository<CreditLimit, Long> {
    Optional<CreditLimit> findByClient(Client client);
}
