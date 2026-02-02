package com.example.demo.controller;

import com.example.demo.base.entity.Client;
import com.example.demo.base.entity.ClientStatus;
import com.example.demo.base.entity.CreditLimit;
import com.example.demo.base.repository.CreditLimitExcelRepository;
import com.example.demo.base.repository.ClientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CreditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CreditLimitExcelRepository creditLimitRepository;

    @Test
    void shouldCreateAndGetCreditLimit() throws Exception {
        // Створюємо клієнта прямо в базі
        Client client = new Client.Builder("ext-1", "Alice", ClientStatus.ACTIVE).build();
        client = clientRepository.save(client);
        String body = """
                {
                  "limitAmount": 1000,
                  "paymentTermsDays": 30
                }
                """;
        mockMvc.perform(post("/clients/ext-1/credit-limit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.limitAmount").value(1000))
                .andExpect(jsonPath("$.usedAmount").value(0));
        mockMvc.perform(get("/clients/ext-1/credit-limit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.limitAmount").value(1000))
                .andExpect(jsonPath("$.usedAmount").value(0));

        CreditLimit cl = creditLimitRepository.findByClient(client).orElseThrow();
        assertThat(cl.getLimitAmount()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(cl.getUsedAmount()).isEqualTo(BigDecimal.ZERO);
    }
}
