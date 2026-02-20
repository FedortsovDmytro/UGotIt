package com.example.demo.base.service;


import com.example.demo.base.base.entity.Bisnode;
import com.example.demo.base.base.entity.Client;
import com.example.demo.base.base.service.BisnodeService;
import com.example.demo.base.excelUpload.repository.BisnodeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BisnodeServiceTest {

    @Mock
    private BisnodeRepository repository;

    @InjectMocks
    private BisnodeService service;

    @Test
    void shouldReturnLatestBisnodeWhenExists() {
        Client client = new Client();
        Bisnode bisnode = new Bisnode();

        when(repository.findTopByClientOrderByFetchedAtDesc(client))
                .thenReturn(Optional.of(bisnode));

        Bisnode result = service.findLatestByClient(client);

        assertNotNull(result);
        assertEquals(bisnode, result);
        verify(repository).findTopByClientOrderByFetchedAtDesc(client);
    }

    @Test
    void shouldReturnNullWhenBisnodeNotExists() {
        Client client = new Client();

        when(repository.findTopByClientOrderByFetchedAtDesc(client))
                .thenReturn(Optional.empty());

        Bisnode result = service.findLatestByClient(client);

        assertNull(result);
        verify(repository).findTopByClientOrderByFetchedAtDesc(client);
    }
}