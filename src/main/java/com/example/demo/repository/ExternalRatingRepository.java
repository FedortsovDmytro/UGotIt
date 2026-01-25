package com.example.demo.repository;

import com.example.demo.entity.Client;
import com.example.demo.entity.ExternalRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExternalRatingRepository extends JpaRepository<ExternalRating, Long> {
    List<ExternalRating> findByClient(Client client);
}
