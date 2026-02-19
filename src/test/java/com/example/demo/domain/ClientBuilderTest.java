//package com.example.demo.domain;
//
//import com.example.demo.base.entity.Client;
//import com.example.demo.base.entity.ClientStatus;
//import org.junit.jupiter.api.Test;
//
//import java.math.BigDecimal;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//
//class ClientBuilderTest {
//
//    @Test
//    void shouldBuildClientWithRequiredFields() {
//        Client client = Client.builder(
//                        "EXT-123",
//                        "ACME Sp. z o.o.",
//                        ClientStatus.ACTIVE
//                )
//                .ratingExternal("B")
//                .creditLimit(BigDecimal.valueOf(50_000))
//                .build();
//
//        assertEquals("EXT-123", client.getExternalId());
//        assertEquals(ClientStatus.ACTIVE, client.getStatus());
//        assertEquals("B", client.getRatingExternal());
//    }
//
//    @Test
//    void shouldThrowExceptionWhenRequiredFieldMissing() {
//        assertThrows(IllegalStateException.class, () ->
//                Client.builder(
//                        null,
//                        "ACME",
//                        ClientStatus.ACTIVE
//                ).build()
//        );
//    }
//}
