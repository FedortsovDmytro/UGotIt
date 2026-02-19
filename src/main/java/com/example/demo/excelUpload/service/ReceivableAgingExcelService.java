package com.example.demo.excelUpload.service;

import com.example.demo.base.entity.Client;
import com.example.demo.base.entity.ClientStatus;
import com.example.demo.base.entity.ReceivableAging;
import com.example.demo.base.repository.ClientRepository;
import com.example.demo.excelUpload.repository.ReceivableAgingRepository;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;


@Service
public class ReceivableAgingExcelService {

    private final ClientRepository clientRepository;
    private final ReceivableAgingRepository repository;

    public ReceivableAgingExcelService(
            ClientRepository clientRepository,
            ReceivableAgingRepository repository
    ) {
        this.clientRepository = clientRepository;
        this.repository = repository;
    }
    @Transactional
    public void importFile(File file, LocalDate reportDate) throws IOException {
        try (Workbook wb = WorkbookFactory.create(file)) {
            Sheet sheet = wb.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String externalId = readAsString(row.getCell(0));

                Client client = clientRepository
                        .findByExternalId(externalId)
                        .orElseGet(() -> {
                            Client c = new Client();
                            c.setExternalId(externalId);
                            c.setFullName("AUTO_IMPORTED_" + externalId);
                            c.setStatus(ClientStatus.ACTIVE);
                            return clientRepository.save(c);
                        });

                ReceivableAging ra = new ReceivableAging();
                ra.setClient(client);
                ra.setReportDate(reportDate);

                ra.setTotalAmount(read(row, 2));
                ra.setNotDue(read(row, 3));
                ra.setOverdue1to7(read(row, 4));
                ra.setOverdue8to14(read(row, 5));
                ra.setOverdue15to30(read(row, 6));
                ra.setOverdue31to60(read(row, 7));
                ra.setOverdue61to90(read(row, 8));
                ra.setOverdue91to120(read(row, 9));
                ra.setOverdue121to360(read(row, 10));
                ra.setOverdueAbove360(read(row, 11));

                ra.setPaymentTermsDays(parseDays(row.getCell(12)));
                ra.setLegalEvents(readString(row.getCell(13)));


                repository.save(ra);
            }
        }
    }
    private String readAsString(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                yield String.valueOf((long) cell.getNumericCellValue());
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }


    private String readString(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> null;
        };
    }
    //
//    private BigDecimal read(Row r, int idx) {
//        Cell cell = r.getCell(idx);
//        if (cell == null) return BigDecimal.ZERO;
//
//        return BigDecimal.valueOf(cell.getNumericCellValue());
//    }
    private BigDecimal read(Row r, int idx) {
        Cell cell = r.getCell(idx);
        if (cell == null) return BigDecimal.ZERO;

        return switch (cell.getCellType()) {
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING -> {
                String val = cell.getStringCellValue().replace(",", ".").trim();
                yield val.isEmpty() ? BigDecimal.ZERO : new BigDecimal(val);
            }
            default -> BigDecimal.ZERO;
        };
    }

    private Integer parseDays(Cell cell) {
        if (cell == null) return null;

        String raw = switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            default -> null;
        };

        if (raw == null) return null;

        String digits = raw.replaceAll("[^0-9]", "");

        if (digits.isEmpty()) {
            return null;
        }

        return Integer.parseInt(digits);
    }

}