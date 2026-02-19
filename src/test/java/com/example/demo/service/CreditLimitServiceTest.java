//package com.example.demo.service;
//
//import com.example.demo.base.entity.Client;
//import com.example.demo.base.entity.CreditLimit;
//import com.example.demo.base.entity.ClientStatus;
//import com.example.demo.base.repository.ClientRepository;
//import com.example.demo.base.repository.CreditLimitExcelRepository;
//import com.example.demo.base.service.CreditLimitService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.math.BigDecimal;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.*;
//class CreditLimitServiceTest {
//
//    private CreditLimitExcelRepository creditLimitRepository;
//    private ClientRepository clientRepository;
//    private CreditLimitService creditLimitService;
//
//    private Client client;
//
//    @BeforeEach
//    void setUp() {
//        creditLimitRepository = mock(CreditLimitExcelRepository.class);
//        clientRepository = mock(ClientRepository.class);
//        creditLimitService = new CreditLimitService(
//                creditLimitRepository,
//                clientRepository
//        );
//
//        client = Client.builder("ext-1", "Alice", ClientStatus.ACTIVE).build();
//    }
//
//    @Test
//    void createCreditLimit_shouldSave() {
//        when(clientRepository.findByExternalId("ext-1"))
//                .thenReturn(Optional.of(client));
//
//        when(creditLimitRepository.save(any()))
//                .thenAnswer(inv -> inv.getArgument(0));
//
//        CreditLimit cl = creditLimitService.createCreditLimit(
//                "ext-1",
//                BigDecimal.valueOf(1000),
//                30
//        );
//
//        assertThat(cl.getLimitAmount()).isEqualTo(BigDecimal.valueOf(1000));
//        verify(creditLimitRepository).save(any());
//    }
//
//    @Test
//    void canPlaceOrder_shouldReturnTrueOrFalse() {
//        CreditLimit cl = new CreditLimit.Builder(client, BigDecimal.valueOf(1000), 30)
//                .usedAmount(BigDecimal.valueOf(500))
//                .build();
//
//        when(clientRepository.findByExternalId("ext-1"))
//                .thenReturn(Optional.of(client));
//        when(creditLimitRepository.findByClient(client))
//                .thenReturn(Optional.of(cl));
//
//        assertThat(creditLimitService.canPlaceOrder("ext-1", BigDecimal.valueOf(400)))
//                .isTrue();
//        assertThat(creditLimitService.canPlaceOrder("ext-1", BigDecimal.valueOf(600)))
//                .isFalse();
//    }
//}
