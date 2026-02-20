package com.example.demo.base.excelUpload.service;

import com.example.demo.base.base.entity.Client;
import com.example.demo.base.base.entity.ClientStatus;
import com.example.demo.base.base.entity.CreditLimit;
import com.example.demo.base.excelUpload.repository.CreditLimitRepository;
import com.example.demo.base.base.repository.ClientRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;

@Service
public class CreditLimitExcelService {

    private final ClientRepository clientRepo;
    private final CreditLimitRepository creditLimitRepo;

    public CreditLimitExcelService(ClientRepository clientRepo,
                                   CreditLimitRepository creditLimitRepo) {
        this.clientRepo = clientRepo;
        this.creditLimitRepo = creditLimitRepo;
    }

    public void importFile(File file) {
        try (Workbook workbook = WorkbookFactory.create(new FileInputStream(file))) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {

                String externalId = getString(row, 0);

                if (externalId == null ||
                        externalId.isBlank() ||
                        externalId.equalsIgnoreCase("konto")) {
                    continue;
                }

                var clientOpt = clientRepo.findByExternalId(externalId);
//                if (clientOpt.isEmpty()) {
//                    System.out.println("Клієнта не знайдено, пропущено: " + externalId);
//                    continue;
//                }

                Client client = clientOpt.orElseGet(() -> {
                    Client newClient = new Client();
                    newClient.setExternalId(externalId);
                    newClient.setFullName(getString(row, 1));
                    newClient.setStatus(ClientStatus.ACTIVE);
                    return clientRepo.save(newClient);
                });


                BigDecimal limit = getDecimal(row, 6);
                BigDecimal used = getDecimal(row, 15);
                Integer days = getInteger(row, 9);

                if (days == null || days <= 0) {
                    System.out.println("Некоректні days для: " + externalId);
                    continue;
                }

                CreditLimit creditLimit = creditLimitRepo
                        .findByClient(client)
                        .orElseGet(() ->
                                new CreditLimit.Builder(client, limit, days)
                                        .usedAmount(BigDecimal.ZERO)
                                        .build()
                        );

                creditLimit.setUsedAmount(used);
                creditLimitRepo.save(creditLimit);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to import credit limits", e);
        }
    }

    private String getString(Row row, int index) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;

        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private BigDecimal getDecimal(Row row, int index) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return BigDecimal.ZERO;

        return switch (cell.getCellType()) {
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING -> {
                String value = cell.getStringCellValue().trim();
                yield value.isEmpty()
                        ? BigDecimal.ZERO
                        : new BigDecimal(value.replace(",", "."));
            }
            default -> BigDecimal.ZERO;
        };
    }

    private Integer getInteger(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }

        if (cell.getCellType() == CellType.STRING) {
            String value = cell.getStringCellValue();
            value = value.replaceAll("[^0-9]", "");
            if (value.isEmpty()) return null;
            return Integer.parseInt(value);
        }

        return null;
    }

}