package com.example.demo.uploading.archive;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArchivedClientRepository extends JpaRepository<ArchivedClient, Long> {

    List<ArchivedClient> findByExternalId(String externalId);


}
