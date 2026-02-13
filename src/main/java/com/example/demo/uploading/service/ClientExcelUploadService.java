package com.example.demo.uploading;

import com.example.demo.uploading.dto.ReceivableRecord;
import com.example.demo.uploading.dto.ReceivableAgingBuilder;
import com.example.demo.risk.AgingResult;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ClientExcelUploadService {

    public AgingResult processExcel(String clientId, String clientName, MultipartFile file) throws Exception {
        List<ReceivableRecord> records = new ArrayList<>();

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // skip header

                String externalId = getCellString(row.getCell(0));
                Integer daysPastDue = getCellInteger(row.getCell(1));
                BigDecimal saldo = getCellDecimal(row.getCell(2));

                if (externalId != null && daysPastDue != null && saldo != null) {
                    records.add(new ReceivableRecord(externalId, daysPastDue, saldo));
                }
            }
        }

        AgingResult agingResult = ReceivableAgingBuilder.build(records);


        return agingResult;
    }

    private String getCellString(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((long)cell.getNumericCellValue());
        return null;
    }

    private Integer getCellInteger(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) return (int) cell.getNumericCellValue();
        if (cell.getCellType() == CellType.STRING) {
            try { return Integer.parseInt(cell.getStringCellValue()); } catch (Exception e) { return null; }
        }
        return null;
    }

    private BigDecimal getCellDecimal(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) return BigDecimal.valueOf(cell.getNumericCellValue());
        if (cell.getCellType() == CellType.STRING) {
            try { return new BigDecimal(cell.getStringCellValue()); } catch (Exception e) { return null; }
        }
        return null;
    }
}
