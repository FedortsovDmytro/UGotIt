package com.example.demo.ekselUploud.reposytory;

import com.example.demo.entity.BisnodeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BisnodeRepository extends JpaRepository<Bisnode, Long> {
    Optional<Bisnode> findByExternalId(Long externalId);
}
