package com.example.demo.controller;

import com.example.demo.base.controller.ClientController;
import com.example.demo.base.entity.Client;
import com.example.demo.base.entity.ClientStatus;
import com.example.demo.base.service.ClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientService clientService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateClient() throws Exception {
        Client client = Client.builder("ext-1", "Alice", ClientStatus.ACTIVE).build();

        when(clientService.createClient("ext-1", "Alice", ClientStatus.ACTIVE))
                .thenReturn(client);

        String body = """
                {
                  "externalId": "ext-1",
                  "fullName": "Alice",
                  "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())           // 201
                .andExpect(jsonPath("$.externalId").value("ext-1"))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldGetClientByExternalId() throws Exception {
        Client client = Client.builder("ext-1", "Alice", ClientStatus.ACTIVE).build();

        when(clientService.getByExternalId("ext-1")).thenReturn(client);

        mockMvc.perform(get("/clients/ext-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value("ext-1"))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldSearchClientsByName() throws Exception {
        Client client = Client.builder("ext-1", "Alice", ClientStatus.ACTIVE).build();

        when(clientService.searchByName("Ali"))
                .thenReturn(List.of(client));

        mockMvc.perform(get("/clients/search")
                        .param("name", "Ali"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].externalId").value("ext-1"))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }
}