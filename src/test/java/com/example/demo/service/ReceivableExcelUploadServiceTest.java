package com.example.demo.service;


import com.example.demo.base.entity.Client;
import com.example.demo.uploading.dto.ReceivableRecord;
import com.example.demo.uploading.dto.ReceivableRepository;
import com.example.demo.uploading.service.ReceivableExcelUploadService;
import com.example.demo.uploading.service.RiskProcessingService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ReceivableExcelUploadServiceTest {

    private ReceivableRepository receivableRepository;
    private RiskProcessingService riskProcessingService;
    private ReceivableExcelUploadService service;

    @BeforeEach
    void setUp() {
        receivableRepository = mock(ReceivableRepository.class);
        riskProcessingService = mock(RiskProcessingService.class);
        service = new ReceivableExcelUploadService(receivableRepository, riskProcessingService);
    }

    @Test
    void testUploadForClient_processesExcelAndCallsRiskProcessing() throws Exception {
        Client client = new Client();
        client.setExternalId("EXT001");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet();
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ExternalId");
            header.createCell(1).setCellValue("Type");
            header.createCell(2).setCellValue("Rating");

            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("R001");
            row1.createCell(1).setCellValue("TypeA");
            row1.createCell(2).setCellValue("AAA");

            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("R002");
            row2.createCell(1).setCellValue("TypeB");
            row2.createCell(2).setCellValue("BBB");

            workbook.write(baos);
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(baos.toByteArray());

        // Capture the records sent to riskProcessingService
        ArgumentCaptor<List<ReceivableRecord>> captor = ArgumentCaptor.forClass(List.class);

        service.uploadForClient(client, inputStream);

        verify(riskProcessingService, times(1)).recalculate(eq(client), captor.capture());

        List<ReceivableRecord> records = captor.getValue();
        assertEquals(2, records.size());

        ReceivableRecord rec1 = records.get(0);
        assertEquals("R001", rec1.getExternalId());
        assertEquals(0, rec1.getDaysPastDue());
        assertEquals(0, rec1.getSaldo().compareTo(BigDecimal.ZERO));

        ReceivableRecord rec2 = records.get(1);
        assertEquals("R002", rec2.getExternalId());
        assertEquals(0, rec2.getDaysPastDue());
        assertEquals(0, rec2.getSaldo().compareTo(BigDecimal.ZERO));
    }
}