package com.example.demo.uploading;

import com.example.demo.base.entity.Client;
import com.example.demo.base.repository.ClientRepository;
import com.example.demo.uploading.dto.ReceivableRecord;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReceivableExcelUploadService {

    private final ClientRepository clientRepository;
    private final RiskProcessingService riskProcessingService;

    public ReceivableExcelUploadService(
            ClientRepository clientRepository,
            RiskProcessingService riskProcessingService
    ) {
        this.clientRepository = clientRepository;
        this.riskProcessingService = riskProcessingService;
    }

    public void uploadAndProcess(InputStream is) {

        List<ReceivableRecord> records = parseExcel(is);

        Map<String, List<ReceivableRecord>> grouped =
                records.stream()
                        .collect(Collectors.groupingBy(ReceivableRecord::getExternalId));

        for (var entry : grouped.entrySet()) {

            Client client = clientRepository
                    .findByExternalId(entry.getKey())
                    .orElseThrow();

            riskProcessingService.recalculate(client, entry.getValue());
        }
    }

    private List<ReceivableRecord> parseExcel(InputStream is) {

        List<ReceivableRecord> records = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            Row headerRow = rows.next();
            Map<String, Integer> headers = mapHeaders(headerRow);

            while (rows.hasNext()) {
                Row row = rows.next();
                records.add(toRecord(row, headers));
            }

        } catch (Exception e) {
            throw new RuntimeException("Excel parsing failed", e);
        }

        return records;
    }

    private Map<String, Integer> mapHeaders(Row headerRow) {
        Map<String, Integer> headers = new HashMap<>();
        for (Cell cell : headerRow) {
            headers.put(
                    cell.getStringCellValue().trim().toLowerCase(),
                    cell.getColumnIndex()
            );
        }
        return headers;
    }

    private Integer getHeader(Map<String, Integer> headers, String name) {
        Integer idx = headers.get(name.toLowerCase());
        if (idx == null)
            throw new IllegalArgumentException("Missing column: " + name);
        return idx;
    }

    private ReceivableRecord toRecord(Row row, Map<String, Integer> headers) {
        return new ReceivableRecord(
                getString(row, getHeader(headers, "client id")),
                getInt(row, getHeader(headers, "dni po terminie")),
                getDecimal(row, getHeader(headers, "saldo"))
        );
    }

    private String getString(Row row, int idx) {
        Cell c = row.getCell(idx);
        if (c == null) return null;

        if (c.getCellType() == CellType.STRING)
            return c.getStringCellValue().trim();

        if (c.getCellType() == CellType.NUMERIC)
            return String.valueOf((long) c.getNumericCellValue());

        return null;
    }

    private Integer getInt(Row row, int idx) {
        Cell c = row.getCell(idx);
        return c == null ? null : (int) c.getNumericCellValue();
    }

    private java.math.BigDecimal getDecimal(Row row, int idx) {
        Cell c = row.getCell(idx);
        return c == null ? null :
                java.math.BigDecimal.valueOf(c.getNumericCellValue());
    }
}
