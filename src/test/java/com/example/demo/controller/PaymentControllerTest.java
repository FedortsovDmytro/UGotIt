//package com.example.demo.controller;
//
//import com.example.demo.base.controller.PaymentController;
//import com.example.demo.base.entity.*;
//import com.example.demo.base.service.ClientService;
//import com.example.demo.base.service.InvoiceService;
//import com.example.demo.base.service.PaymentService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//
//import org.springframework.http.MediaType;
//
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//@WebMvcTest(PaymentController.class)
//class PaymentControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private PaymentService paymentService;
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
//    void shouldCreatePayment() throws Exception {
//        Client client = Client.builder("ext-1", "Alice", ClientStatus.ACTIVE).build();
//
//        Invoice invoice = new Invoice.Builder(
//                client,
//                LocalDate.now(),
//                LocalDate.now().plusDays(10),
//                BigDecimal.valueOf(1000),
//                InvoiceStatus.OPEN
//        ).build();
//
//        Payment payment = new Payment.Builder(
//                invoice,
//                LocalDate.now(),
//                BigDecimal.valueOf(500)
//        ).method("BANK_TRANSFER").build();
//
//        when(clientService.getByExternalId("ext-1")).thenReturn(client);
//        when(invoiceService.getInvoice(1L)).thenReturn(invoice);
//        when(paymentService.createPayment(
//                invoice,
//                BigDecimal.valueOf(500),
//                LocalDate.now(),
//                "BANK_TRANSFER"
//        )).thenReturn(payment);
//
//        String body = """
//                {
//                  "amount": 500,
//                  "paymentDate": "%s",
//                  "method": "BANK_TRANSFER"
//                }
//                """.formatted(LocalDate.now());
//
//        mockMvc.perform(
//                        post("/clients/ext-1/invoices/1/payments")
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(body)
//                )
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.amount").value(500))
//                .andExpect(jsonPath("$.method").value("BANK_TRANSFER"));
//    }
//}
