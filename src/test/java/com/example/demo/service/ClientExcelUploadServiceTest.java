package com.example.demo.service;


import com.example.demo.base.dto.UploadResult;
import com.example.demo.base.entity.*;
import com.example.demo.base.repository.ClientRepository;
import com.example.demo.base.repository.RiskAssessmentRepository;
import com.example.demo.excelUpload.repository.CreditLimitRepository;
import com.example.demo.excelUpload.repository.ReceivableAgingRepository;
import com.example.demo.risk.RiskLevel;
import com.example.demo.uploading.archive.ArchiveService;
import com.example.demo.uploading.service.ClientAlreadyExistsException;
import com.example.demo.uploading.service.ClientExcelUploadService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientExcelUploadServiceTest {

    private ClientRepository clientRepository;
    private RiskAssessmentRepository riskRepository;
    private ArchiveService archiveService;
    private CreditLimitRepository creditLimitRepository;
    private ReceivableAgingRepository agingRepository;
    private ClientExcelUploadService service;

    @BeforeEach
    void setUp() {
        clientRepository = mock(ClientRepository.class);
        riskRepository = mock(RiskAssessmentRepository.class);
        archiveService = mock(ArchiveService.class);
        creditLimitRepository = mock(CreditLimitRepository.class);
        agingRepository = mock(ReceivableAgingRepository.class);

        service = new ClientExcelUploadService(
                clientRepository,
                riskRepository,
                archiveService,
                creditLimitRepository,
                agingRepository
        );
    }

    @Test
    void testUploadAndReturnResult_createsNewClientAndCalculatesRisk() throws Exception {
        String clientId = "EXT001";
        String clientName = "Test Client";

        // Build an in-memory Excel file
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet();
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("amount");
            header.createCell(1).setCellValue("days overdue");

            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue(50000);
            row1.createCell(1).setCellValue(10);

            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue(60000);
            row2.createCell(1).setCellValue(100);

            workbook.write(baos);
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(baos.toByteArray());

        // Mock repository behaviors
        when(clientRepository.findByExternalId(clientId)).thenReturn(Optional.empty());
        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
        when(clientRepository.save(clientCaptor.capture())).thenAnswer(i -> i.getArguments()[0]);

        ArgumentCaptor<RiskAssessment> riskCaptor = ArgumentCaptor.forClass(RiskAssessment.class);
        when(riskRepository.save(riskCaptor.capture())).thenAnswer(i -> i.getArguments()[0]);

        UploadResult result = null;
        try {
            result = service.uploadAndReturnResult(inputStream, clientId, clientName, true, Locale.ENGLISH);
        } catch (ClientAlreadyExistsException e) {
            throw new RuntimeException(e);
        }

        // Verify client saved
        Client savedClient = clientCaptor.getValue();
        assertEquals(clientId, savedClient.getExternalId());
        assertEquals(clientName, savedClient.getName());
        assertEquals(ClientStatus.ACTIVE, savedClient.getStatus());

        // Verify risk saved
        RiskAssessment risk = riskCaptor.getValue();
        assertEquals(savedClient, risk.getClient());
        assertEquals(80, risk.getRiskScore()); // Correct score based on logic: exposure + maxDaysOverdue
        assertEquals(RiskLevel.HIGH, risk.getRiskLevel());

        // Verify result DTO
        assertEquals(clientId, result.getClientId());
        assertEquals(clientName, result.getClientName());
        assertEquals(new BigDecimal("0"), result.getCreditLimit()); // default zero
        assertEquals(0, result.getExposure().compareTo(new BigDecimal("110000")));
        assertEquals(80, result.getRiskScore());
        assertEquals("HIGH", result.getRiskLevel());
        assertNotNull(result.getReasons());
        assertNotNull(result.getRecommendation());

        // Verify repositories called
        verify(creditLimitRepository, times(1)).save(any(CreditLimit.class));
        verify(agingRepository, times(1)).save(any(ReceivableAging.class));
    }
}