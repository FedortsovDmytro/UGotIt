package com.example.demo.base.service;

import com.example.demo.base.entity.Bisnode;
import com.example.demo.base.entity.Client;
import com.example.demo.excelUpload.repository.BisnodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BisnodeService {

    private final BisnodeRepository repository;
    public BisnodeService(BisnodeRepository repository) {
        this.repository = repository;
    }

    public Bisnode findLatestByClient(Client client) {
        return repository
                .findTopByClientOrderByFetchedAtDesc(client)
                .orElse(null);
    }
}
