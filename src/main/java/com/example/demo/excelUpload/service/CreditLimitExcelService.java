package com.example.demo.excelUpload.servise;

import com.example.demo.entity.Client;
import com.example.demo.entity.CreditLimit;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.CreditLimitExcelRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

@Service
public class CreditLimitExcelService {

    private final ClientRepository clientRepo;
    private final CreditLimitExcelRepository creditLimitRepo;

    public CreditLimitExcelService(
            ClientRepository clientRepo,
            CreditLimitExcelRepository creditLimitRepo
    ) {
        this.clientRepo = clientRepo;
        this.creditLimitRepo = creditLimitRepo;
    }

    public void importFile(File file) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() < 2) continue;

                String externalId = getString(row, 0);
                if (externalId == null || externalId.isBlank()) continue;

                Client client = clientRepo.findByExternalId(externalId)
                        .orElseThrow(() ->
                                new IllegalStateException("Client not found: " + externalId));

                BigDecimal limitAmount = getDecimal(row, 6);
                BigDecimal usedAmount  = getDecimal(row, 15);
                int paymentTermsDays   = getInt(row, 9);

                if (limitAmount == null) continue;

                CreditLimit creditLimit = creditLimitRepo
                        .findByClient(client)
                        .orElse(
                                new CreditLimit.Builder(
                                        client,
                                        limitAmount,
                                        paymentTermsDays
                                )
                                        .usedAmount(
                                                usedAmount != null ? usedAmount : BigDecimal.ZERO
                                        )
                                        .build()
                        );

                creditLimit.setUsedAmount(
                        usedAmount != null ? usedAmount : BigDecimal.ZERO
                );

                creditLimitRepo.save(creditLimit);
            }
        }
    }


    private String getString(Row row, int idx) {
        Cell cell = row.getCell(idx);
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> null;
        };
    }

    private BigDecimal getDecimal(Row row, int idx) {
        Cell cell = row.getCell(idx);
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING -> {
                String v = cell.getStringCellValue().replace(",", ".").trim();
                if (v.isEmpty()) yield null;
                yield new BigDecimal(v);
            }
            default -> null;
        };
    }

    private int getInt(Row row, int idx) {
        Cell cell = row.getCell(idx);
        if (cell == null) return 0;

        return switch (cell.getCellType()) {
            case NUMERIC -> (int) cell.getNumericCellValue();
            case STRING -> {
                String v = cell.getStringCellValue()
                        .replace(" dni", "")
                        .trim();
                yield v.isEmpty() ? 0 : Integer.parseInt(v);
            }
            default -> 0;
        };
    }
}
