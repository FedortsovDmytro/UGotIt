package com.example.demo.ekselUploud.servise;

import org.springframework.stereotype.Repository;

@Repository
public interface BisnodeRepository extends JpaRepository<Bisnode, Long> {
    Optional<Bisnode> findByExternalId(Long externalId);
}
