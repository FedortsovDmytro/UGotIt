package com.example.demo.base.uploading.dto;

import com.example.demo.base.base.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceivableRepository extends JpaRepository<Receivable, Long> {

    List<Receivable> findByClient(Client client);

    void deleteByClient(Client client);
}
