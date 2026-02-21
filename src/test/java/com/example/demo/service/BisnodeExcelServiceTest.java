package com.example.demo.service;


import com.example.demo.base.entity.*;
import com.example.demo.base.repository.ClientRepository;
import com.example.demo.excelUpload.repository.BisnodeRepository;
import com.example.demo.excelUpload.service.BisnodeExcelService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BisnodeExcelServiceTest {

    private BisnodeRepository bisnodeRepository;
    private ClientRepository clientRepository;
    private BisnodeExcelService service;

    @BeforeEach
    void setUp() {
        bisnodeRepository = mock(BisnodeRepository.class);
        clientRepository = mock(ClientRepository.class);
        service = new BisnodeExcelService(bisnodeRepository, clientRepository);
    }

    @Test
    void testImportBisnodeFile_createsNewClientAndBisnode() throws IOException {
        // Create temporary Excel file with test data
        File tempFile = File.createTempFile("bisnode_test", ".xlsx");
        tempFile.deleteOnExit();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet();
            // Header row
            sheet.createRow(0).createCell(0).setCellValue("externalId");
            // Data row
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("EXT001");
            row.createCell(1).setCellValue("Test Client");
            row.createCell(2).setCellValue(42);
            row.createCell(3).setCellValue("AAA");

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                workbook.write(fos);
            }
        }

        // Mock repository behavior
        when(clientRepository.findByExternalId("EXT001")).thenReturn(Optional.empty());
        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
        when(clientRepository.save(clientCaptor.capture())).thenAnswer(i -> i.getArguments()[0]);

        ArgumentCaptor<Bisnode> bisnodeCaptor = ArgumentCaptor.forClass(Bisnode.class);
        when(bisnodeRepository.save(bisnodeCaptor.capture())).thenAnswer(i -> i.getArguments()[0]);

        // Call method
        service.importBisnodeFile(tempFile);

        // Verify Client creation
        Client createdClient = clientCaptor.getValue();
        assertEquals("EXT001", createdClient.getExternalId());
        assertEquals("Test Client", createdClient.getName());
        assertEquals(ClientStatus.ACTIVE, createdClient.getStatus());
        assertEquals(LocalDate.now(), createdClient.getCreatedAt());

        // Verify Bisnode creation
        Bisnode createdBisnode = bisnodeCaptor.getValue();
        assertEquals("Test Client", createdBisnode.getClientName());
        assertEquals(42, createdBisnode.getDax());
        assertEquals("AAA", createdBisnode.getRating());
        assertEquals(createdClient, createdBisnode.getClient());
    }

    @Test
    void testImportBisnodeFile_existingClient() throws IOException {
        File tempFile = File.createTempFile("bisnode_test2", ".xlsx");
        tempFile.deleteOnExit();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet();
            sheet.createRow(0).createCell(0).setCellValue("externalId");
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("EXT002");
            row.createCell(1).setCellValue("Existing Client");
            row.createCell(2).setCellValue(100);
            row.createCell(3).setCellValue("BBB");

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                workbook.write(fos);
            }
        }

        Client existingClient = Client.builder("EXT002", "Existing Client", ClientStatus.ACTIVE).build();
        when(clientRepository.findByExternalId("EXT002")).thenReturn(Optional.of(existingClient));
        when(bisnodeRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        service.importBisnodeFile(tempFile);

        // Verify repository.save(Client) is never called because client exists
        verify(clientRepository, never()).save(any());

        // Verify Bisnode saved
        verify(bisnodeRepository, times(1)).save(any());
    }

    @Test
    void testImportBisnodeFile_skipsEmptyExternalId() throws IOException {
        File tempFile = File.createTempFile("bisnode_test3", ".xlsx");
        tempFile.deleteOnExit();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet();
            sheet.createRow(0).createCell(0).setCellValue("externalId");
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue(""); // empty externalId
            row.createCell(1).setCellValue("Should be skipped");

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                workbook.write(fos);
            }
        }

        service.importBisnodeFile(tempFile);

        verify(clientRepository, never()).save(any());
        verify(bisnodeRepository, never()).save(any());
    }
}