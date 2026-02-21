package com.example.demo.service;


import com.example.demo.base.entity.Bisnode;
import com.example.demo.base.entity.Client;
import com.example.demo.base.service.BisnodeService;
import com.example.demo.excelUpload.repository.BisnodeRepository;
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