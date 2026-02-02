package com.example.demo.base.repository;

import com.example.demo.base.entity.Client;
import com.example.demo.base.entity.ExternalRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExternalRatingRepository extends JpaRepository<ExternalRating, Long> {
    List<ExternalRating> findByClient(Client client);
}
