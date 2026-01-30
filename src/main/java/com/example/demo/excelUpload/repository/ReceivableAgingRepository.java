package com.example.demo.ekselUploud.repository;

import com.example.demo.entity.ReceivableAging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceivableAgingRepository
        extends JpaRepository<ReceivableAging, Long> {
}
