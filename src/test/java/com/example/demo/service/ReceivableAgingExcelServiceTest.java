package com.example.demo.service;

import com.example.demo.base.entity.Client;
import com.example.demo.base.entity.ClientStatus;
import com.example.demo.base.entity.ReceivableAging;
import com.example.demo.base.repository.ClientRepository;
import com.example.demo.excelUpload.repository.ReceivableAgingRepository;
import com.example.demo.excelUpload.service.ReceivableAgingExcelService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReceivableAgingExcelServiceTest {

    private ClientRepository clientRepository;
    private ReceivableAgingRepository repository;
    private ReceivableAgingExcelService service;

    @BeforeEach
    void setUp() {
        clientRepository = mock(ClientRepository.class);
        repository = mock(ReceivableAgingRepository.class);
        service = new ReceivableAgingExcelService(clientRepository, repository);
    }

    @Test
    void testImportFile_createsNewClientAndReceivableAging() throws Exception {
        File tempFile = File.createTempFile("receivableaging_test", ".xlsx");
        tempFile.deleteOnExit();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet();
            sheet.createRow(0);
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("EXT001");
            row.createCell(2).setCellValue(1000);
            row.createCell(3).setCellValue(200);
            row.createCell(4).setCellValue(50);
            row.createCell(5).setCellValue(30);
            row.createCell(6).setCellValue(20);
            row.createCell(7).setCellValue(10);
            row.createCell(8).setCellValue(5);
            row.createCell(9).setCellValue(2);
            row.createCell(10).setCellValue(1);
            row.createCell(11).setCellValue(0);
            row.createCell(12).setCellValue(30);
            row.createCell(13).setCellValue("No events");

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                workbook.write(fos);
            }
        }

        when(clientRepository.findByExternalId("EXT001")).thenReturn(Optional.empty());
        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
        when(clientRepository.save(clientCaptor.capture())).thenAnswer(i -> i.getArguments()[0]);

        ArgumentCaptor<ReceivableAging> raCaptor = ArgumentCaptor.forClass(ReceivableAging.class);
        when(repository.save(raCaptor.capture())).thenAnswer(i -> i.getArguments()[0]);

        LocalDate reportDate = LocalDate.of(2026, 2, 20);
        service.importFile(tempFile, reportDate);

        Client createdClient = clientCaptor.getValue();
        assertEquals("EXT001", createdClient.getExternalId());
        assertEquals("AUTO_IMPORTED_EXT001", createdClient.getName());
        assertEquals(ClientStatus.ACTIVE, createdClient.getStatus());

        ReceivableAging ra = raCaptor.getValue();
        assertEquals(createdClient, ra.getClient());
        assertEquals(reportDate, ra.getReportDate());
        assertEquals(0, ra.getTotalAmount().compareTo(new BigDecimal("1000")));
        assertEquals(0, ra.getNotDue().compareTo(new BigDecimal("200")));
        assertEquals(0, ra.getOverdue1to7().compareTo(new BigDecimal("50")));
        assertEquals(0, ra.getOverdue8to14().compareTo(new BigDecimal("30")));
        assertEquals(0, ra.getOverdue15to30().compareTo(new BigDecimal("20")));
        assertEquals(0, ra.getOverdue31to60().compareTo(new BigDecimal("10")));
        assertEquals(0, ra.getOverdue61to90().compareTo(new BigDecimal("5")));
        assertEquals(0, ra.getOverdue91to120().compareTo(new BigDecimal("2")));
        assertEquals(0, ra.getOverdue121to360().compareTo(new BigDecimal("1")));
        assertEquals(0, ra.getOverdueAbove360().compareTo(new BigDecimal("0")));
        assertEquals(30, ra.getPaymentTermsDays());
        assertEquals("No events", ra.getLegalEvents());
    }

    @Test
    void testImportFile_existingClientUpdatesReceivableAging() throws Exception {
        File tempFile = File.createTempFile("receivableaging_test2", ".xlsx");
        tempFile.deleteOnExit();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet();
            sheet.createRow(0);
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("EXT002");
            row.createCell(2).setCellValue(500);
            row.createCell(12).setCellValue(15);

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                workbook.write(fos);
            }
        }

        Client existingClient = Client.builder("EXT002", "Existing Client", ClientStatus.ACTIVE).build();
        when(clientRepository.findByExternalId("EXT002")).thenReturn(Optional.of(existingClient));

        ArgumentCaptor<ReceivableAging> raCaptor = ArgumentCaptor.forClass(ReceivableAging.class);
        when(repository.save(raCaptor.capture())).thenAnswer(i -> i.getArguments()[0]);

        LocalDate reportDate = LocalDate.of(2026, 2, 20);
        service.importFile(tempFile, reportDate);

        ReceivableAging ra = raCaptor.getValue();
        assertEquals(existingClient, ra.getClient());
        assertEquals(0, ra.getTotalAmount().compareTo(new BigDecimal("500")));
        assertEquals(15, ra.getPaymentTermsDays());
    }
}