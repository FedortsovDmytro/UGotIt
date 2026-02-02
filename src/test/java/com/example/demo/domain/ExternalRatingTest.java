package com.example.demo.domain;

import com.example.demo.base.entity.Client;
import com.example.demo.base.entity.ClientStatus;
import com.example.demo.base.entity.ExternalRating;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ExternalRatingTest {

    @Test
    void shouldBuildExternalRatingCorrectly() {
        Client client = new Client.Builder("ext1", "Client1", ClientStatus.ACTIVE).build();

        ExternalRating rating = new ExternalRating.Builder(client, "AAA", LocalDate.now())
                .score(90)
                .recommendation("Monitor")
                .source("Bisnode")
                .build();

        assertEquals(client, rating.getClient());
        assertEquals("AAA", rating.getRating());
        assertEquals(90, rating.getScore());
        assertEquals("Monitor", rating.getRecommendation());
        assertEquals("Bisnode", rating.getSource());
    }

    @Test
    void shouldThrowIfRequiredFieldsMissing() {
        Client client = new Client.Builder("ext1", "Client1", ClientStatus.ACTIVE).build();

        assertThrows(IllegalStateException.class,
                () -> new ExternalRating.Builder(null, "AAA", LocalDate.now()).build());

        assertThrows(IllegalStateException.class,
                () -> new ExternalRating.Builder(client, null, LocalDate.now()).build());

        assertThrows(IllegalStateException.class,
                () -> new ExternalRating.Builder(client, "AAA", null).build());
    }
}
