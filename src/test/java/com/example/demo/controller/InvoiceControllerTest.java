//package com.example.demo.controller;
//
//import com.example.demo.base.controller.InvoiceController;
//import com.example.demo.base.entity.Client;
//import com.example.demo.base.entity.ClientStatus;
//import com.example.demo.base.entity.Invoice;
//import com.example.demo.base.entity.InvoiceStatus;
//import com.example.demo.base.service.InvoiceService;
//import com.example.demo.base.service.ClientService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.List;
//
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(InvoiceController.class)
//class InvoiceControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private InvoiceService invoiceService;
//
//    @MockitoBean
//    private ClientService clientService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Test
//    void shouldCreateInvoice() throws Exception {
//        Client client = Client.builder("ext-1", "Alice", ClientStatus.ACTIVE).build();
//
//        Invoice invoice = new Invoice.Builder(client, LocalDate.now(), LocalDate.now().plusDays(10),
//                BigDecimal.valueOf(1000), InvoiceStatus.OPEN)
//                .invoiceNumber("INV-001")
//                .currency("USD")
//                .build();
//
//        when(clientService.getByExternalId("ext-1")).thenReturn(client);
//        when(invoiceService.createInvoice(client.getId(), "INV-001", LocalDate.now(), LocalDate.now().plusDays(10),
//                BigDecimal.valueOf(1000), "USD", InvoiceStatus.OPEN)).thenReturn(invoice);
//
//        String body = """
//                {
//                  "invoiceNumber": "INV-001",
//                  "issueDate": "%s",
//                  "dueDate": "%s",
//                  "amount": 1000,
//                  "currency": "USD",
//                  "status": "OPEN"
//                }
//                """.formatted(LocalDate.now(), LocalDate.now().plusDays(10));
//
//        mockMvc.perform(post("/clients/ext-1/invoices")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(body))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.invoiceNumber").value("INV-001"))
//                .andExpect(jsonPath("$.currency").value("USD"));
//    }
//
//    @Test
//    void shouldGetInvoicesForClient() throws Exception {
//        Client client = Client.builder("ext-1", "Alice", ClientStatus.ACTIVE).build();
//
//        Invoice invoice = new Invoice.Builder(client, LocalDate.now(), LocalDate.now().plusDays(10),
//                BigDecimal.valueOf(1000), InvoiceStatus.OPEN).build();
//
//        when(clientService.getByExternalId("ext-1")).thenReturn(client);
//        when(invoiceService.getInvoicesForClient(client.getId())).thenReturn(List.of(invoice));
//
//        mockMvc.perform(get("/clients/ext-1/invoices"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].amount").value(1000));
//    }
//}
