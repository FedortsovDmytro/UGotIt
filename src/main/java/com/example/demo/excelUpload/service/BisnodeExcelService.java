package com.example.demo.excelUpload.service;

import com.example.demo.base.repository.ClientRepository;
import com.example.demo.excelUpload.repository.BisnodeRepository;
import com.example.demo.base.entity.Bisnode;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class BisnodeExcelService {

    private final BisnodeRepository repository;

    public BisnodeExcelService(BisnodeRepository repository, ClientRepository clientRepository) {
        this.repository = repository;

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
                if (row.getRowNum() == 0) continue; // header

                String clientName = getString(row, 0);
                Long externalId = getLong(row, 1);
                Integer dax = getInteger(row, 2);
                String rating = getString(row, 3);

                if (clientName == null || clientName.isBlank()) {
                    System.out.println("Skipped row " + row.getRowNum() + " — empty client name");
                    continue;
                }
//String clientName, Client client, Integer dax, String rating, LocalDateTime fetchedAt)
                repository.save(new Bisnode(
                        clientName,
                        externalId,
                        dax,
                        rating
                ));
            }
        }
    }

}
