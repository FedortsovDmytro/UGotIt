package com.example.demo.base.uploading.service;

import com.example.demo.base.base.entity.Client;
import com.example.demo.base.uploading.dto.Receivable;
import com.example.demo.base.uploading.dto.ReceivableRecord;
import com.example.demo.base.uploading.dto.ReceivableRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ReceivableExcelUploadService {

    private final ReceivableRepository receivableRepository;
    private final RiskProcessingService riskProcessingService;

    public ReceivableExcelUploadService(ReceivableRepository receivableRepository,
                                        RiskProcessingService riskProcessingService) {
        this.receivableRepository = receivableRepository;
        this.riskProcessingService = riskProcessingService;
    }

    public void uploadForClient(Client client, InputStream excelInputStream) {
        List<Receivable> receivables = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(excelInputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean firstRow = true;

            for (Row row : sheet) {
                if (firstRow) { // skip header
                    firstRow = false;
                    continue;
                }

                String externalId = getString(row, 0);
                String type = getString(row, 1);
                String rating = getString(row, 2);

                if (externalId == null) continue;

                Receivable receivable = new Receivable();
                receivable.setClient(client);
                receivable.setExternalId(externalId);
                receivable.setType(type);
                receivable.setRating(rating);

                receivables.add(receivable);
            }

            List<ReceivableRecord> records = new ArrayList<>();
            for (Receivable r : receivables) {

                records.add(new ReceivableRecord(
                        r.getExternalId(),
                        r.getDaysPastDue() != null ? r.getDaysPastDue() : 0,
                        r.getSaldo() != null ? r.getSaldo() : BigDecimal.ZERO
                ));
            }

            riskProcessingService.recalculate(client, records);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to process receivables Excel file", e);
        }
    }

    private String getString(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            default:
                return null;
        }
    }
}
