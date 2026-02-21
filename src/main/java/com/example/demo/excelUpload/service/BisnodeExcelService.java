package com.example.demo.excelUpload.service;

import com.example.demo.base.entity.Client;
import com.example.demo.base.entity.ClientStatus;
import com.example.demo.base.repository.ClientRepository;
import com.example.demo.excelUpload.repository.BisnodeRepository;
import com.example.demo.base.entity.Bisnode;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class BisnodeExcelService {

    private final BisnodeRepository repository;
    private final ClientRepository clientRepository;
    public BisnodeExcelService(BisnodeRepository repository, ClientRepository clientRepository) {
        this.repository = repository;
        this.clientRepository = clientRepository;

    }
    private String getString(Row row, int index) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private Integer getInteger(Row row, int index) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case NUMERIC -> (int) cell.getNumericCellValue();
            case STRING -> {
                String v = cell.getStringCellValue().replaceAll("[^0-9]", "");
                yield v.isEmpty() ? null : Integer.parseInt(v);
            }
            default -> null;
        };
    }

    private Long getLong(Row row, int index) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case NUMERIC -> (long) cell.getNumericCellValue();
            case STRING -> {
                String v = cell.getStringCellValue().replaceAll("[^0-9]", "");
                yield v.isEmpty() ? null : Long.parseLong(v);
            }
            default -> null;
        };
    }

    public void importBisnodeFile(File file) throws IOException {

        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String externalId = getString(row, 0);   // 🔥 ID
                String clientName = getString(row, 1);   // 🔥 NAME
                Integer dax = getInteger(row, 2);
                String rating = getString(row, 3);

                if (externalId == null || externalId.isBlank()) {
                    System.out.println("⛔ Row " + row.getRowNum() + " skipped — externalId empty");
                    continue;
                }

                Client client = clientRepository
                        .findByExternalId(externalId)
                        .orElseGet(() -> {
                            Client c = new Client();
                            c.setExternalId(externalId);
                            c.setFullName(clientName);
                            c.setStatus(ClientStatus.ACTIVE);
                            c.setCreatedAt(LocalDate.now());
                            return clientRepository.save(c);
                        });

                Bisnode bisnode = new Bisnode(
                        client.getName(),
                        null,
                        dax,
                        rating
                );

                bisnode.setClient(client);
                bisnode.setFetchedAt(LocalDateTime.now());

                repository.save(bisnode);
            }
        }
    }


}