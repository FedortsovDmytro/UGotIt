package com.example.demo.base.service;

import com.example.demo.base.base.entity.Client;
import com.example.demo.base.base.entity.ClientStatus;
import com.example.demo.base.base.entity.CreditLimit;
import com.example.demo.base.base.repository.ClientRepository;
import com.example.demo.base.base.repository.CreditLimitExcelRepository;
import com.example.demo.base.base.service.CreditLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CreditLimitServiceTest {

    @Mock
    private CreditLimitExcelRepository creditLimitRepository;

    @Mock
    private ClientRepository clientRepository;

    private CreditLimitService creditLimitService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        creditLimitService = new CreditLimitService(creditLimitRepository, clientRepository);
    }

    private Client createActiveClient(String externalId, String fullName) {
        return new Client.Builder(externalId, fullName, ClientStatus.ACTIVE).build();
    }

    @Test
    void shouldCreateCreditLimit() {
        Client client = createActiveClient("ext123", "John Doe");
        when(clientRepository.findByExternalId("ext123")).thenReturn(Optional.of(client));

        CreditLimit cl = new CreditLimit.Builder(client, BigDecimal.valueOf(1000), 30)
                .usedAmount(BigDecimal.ZERO)
                .validFrom(LocalDate.now())
                .validTo(LocalDate.now().plusMonths(1))
                .build();
        when(creditLimitRepository.save(any(CreditLimit.class))).thenReturn(cl);

        CreditLimit result = creditLimitService.createCreditLimit("ext123", BigDecimal.valueOf(1000), 30);

        assertThat(result).isNotNull();
        assertThat(result.getClient()).isEqualTo(client);
        assertThat(result.getLimitAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        verify(creditLimitRepository, times(1)).save(any(CreditLimit.class));
    }

    @Test
    void shouldThrowIfClientNotFoundOnCreate() {
        when(clientRepository.findByExternalId("missing")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> creditLimitService.createCreditLimit("missing", BigDecimal.valueOf(1000), 30));
    }

    @Test
    void shouldCheckCanPlaceOrder() {
        Client client = createActiveClient("ext123", "John Doe");
        CreditLimit cl = new CreditLimit.Builder(client, BigDecimal.valueOf(1000), 30)
                .usedAmount(BigDecimal.valueOf(200))
                .validFrom(LocalDate.now())
                .validTo(LocalDate.now().plusMonths(1))
                .build();

        when(clientRepository.findByExternalId("ext123")).thenReturn(Optional.of(client));
        when(creditLimitRepository.findByClient(Optional.of(client))).thenReturn(Optional.of(cl));

        boolean canPlace = creditLimitService.canPlaceOrder("ext123", BigDecimal.valueOf(500));
        boolean cannotPlace = creditLimitService.canPlaceOrder("ext123", BigDecimal.valueOf(900));

        assertThat(canPlace).isTrue();
        assertThat(cannotPlace).isFalse();
    }

    @Test
    void shouldUpdateUsedAmount() {
        Client client = createActiveClient("ext123", "John Doe");
        CreditLimit cl = new CreditLimit.Builder(client, BigDecimal.valueOf(1000), 30)
                .usedAmount(BigDecimal.valueOf(200))
                .validFrom(LocalDate.now())
                .validTo(LocalDate.now().plusMonths(1))
                .build();

        when(clientRepository.findByExternalId("ext123")).thenReturn(Optional.of(client));
        when(creditLimitRepository.findByClient(Optional.of(client))).thenReturn(Optional.of(cl));

        creditLimitService.updateUsedAmount("ext123", BigDecimal.valueOf(400));

        ArgumentCaptor<CreditLimit> captor = ArgumentCaptor.forClass(CreditLimit.class);
        verify(creditLimitRepository).save(captor.capture());

        assertThat(captor.getValue().getUsedAmount()).isEqualByComparingTo(BigDecimal.valueOf(400));
    }

    @Test
    void shouldGetCreditLimit() {
        Client client = createActiveClient("ext123", "John Doe");
        CreditLimit cl = new CreditLimit.Builder(client, BigDecimal.valueOf(1000), 30)
                .usedAmount(BigDecimal.ZERO)
                .validFrom(LocalDate.now())
                .validTo(LocalDate.now().plusMonths(1))
                .build();

        when(clientRepository.findByExternalId("ext123")).thenReturn(Optional.of(client));
        when(creditLimitRepository.findByClient(Optional.of(client))).thenReturn(Optional.of(cl));

        CreditLimit result = creditLimitService.getCreditLimit("ext123");

        assertThat(result).isNotNull();
        assertThat(result.getClient()).isEqualTo(client);
    }

    @Test
    void shouldReturnNullIfFindByClientNotExists() {
        Client client = createActiveClient("ext123", "John Doe");
        when(creditLimitRepository.findByClient(Optional.ofNullable(client))).thenReturn(Optional.empty());

        CreditLimit result = creditLimitService.findByClient(client);

        assertThat(result).isNull();
    }

    @Test
    void shouldFindByExternalId() {
        Client client = createActiveClient("ext123", "John Doe");
        CreditLimit cl = new CreditLimit.Builder(client, BigDecimal.valueOf(1000), 30)
                .usedAmount(BigDecimal.ZERO)
                .build();

        when(clientRepository.findByExternalId("ext123")).thenReturn(Optional.of(client));
        when(creditLimitRepository.findByClient(Optional.of(client))).thenReturn(Optional.of(cl));

        Optional<CreditLimit> result = creditLimitService.findByExternalId("ext123");

        assertThat(result).isPresent();
        assertThat(result.get().getClient()).isEqualTo(client);
    }
}