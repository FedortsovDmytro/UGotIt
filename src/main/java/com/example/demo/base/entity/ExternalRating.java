package com.example.demo.base.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "external_rating")
public class ExternalRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Client client;

    @Column(nullable = false)
    private String rating;

    private Integer score;

    @Column(nullable = false)
    private LocalDate reportDate;

    private String recommendation;
    private String source;

    protected ExternalRating() {}

    private ExternalRating(Builder b) {
        this.client = b.client;
        this.rating = b.rating;
        this.score = b.score;
        this.reportDate = b.reportDate;
        this.recommendation = b.recommendation;
        this.source = b.source;
    }

    public Long getId() {
        return id;
    }

    public Client getClient() {
        return client;
    }

    public String getRating() {
        return rating;
    }

    public Integer getScore() {
        return score;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public String getSource() {
        return source;
    }

    public static class Builder {
        private final Client client;
        private final String rating;
        private final LocalDate reportDate;

        private Integer score;
        private String recommendation;
        private String source;

        public Builder(Client client, String rating, LocalDate reportDate) {
            this.client = client;
            this.rating = rating;
            this.reportDate = reportDate;
        }

        public Builder score(Integer val) {
            this.score = val;
            return this;
        }

        public Builder recommendation(String val) {
            this.recommendation = val;
            return this;
        }

        public Builder source(String val) {
            this.source = val;
            return this;
        }

        public ExternalRating build() {
            if (client == null) throw new IllegalStateException("client required");
            if (rating == null || rating.isBlank())
                throw new IllegalStateException("rating required");
            if (reportDate == null)
                throw new IllegalStateException("reportDate required");

            return new ExternalRating(this);
        }
    }
}
