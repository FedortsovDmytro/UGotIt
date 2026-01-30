package com.example.demo.ekselUploud.repository;

import com.example.demo.entity.Bisnode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BisnodeRepository extends JpaRepository<Bisnode, Long> {
    Optional<Bisnode> findByExternalId(Long externalId);
}
