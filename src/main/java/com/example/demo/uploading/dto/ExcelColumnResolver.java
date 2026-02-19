package com.example.demo.uploading.dto;


import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;

import java.util.HashMap;
import java.util.Map;

public class ExcelColumnResolver {

    public static Map<String, Integer> resolveColumns(Row headerRow) {

        Map<String, Integer> columnMap = new HashMap<>();

        for (Cell cell : headerRow) {
            String header = cell.getStringCellValue().trim().toLowerCase();
            columnMap.put(header, cell.getColumnIndex());
        }

        return columnMap;
    }
}