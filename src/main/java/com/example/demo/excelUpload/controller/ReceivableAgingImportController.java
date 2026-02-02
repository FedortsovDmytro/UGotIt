package com.example.demo.excelUpload.controller;

import com.example.demo.excelUpload.service.ReceivableAgingExcelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/import")
public class ReceivableAgingImportController {

    private final ReceivableAgingExcelService service;

    public ReceivableAgingImportController(ReceivableAgingExcelService service) {
        this.service = service;
    }

    @PostMapping("/receivable-aging")
    public ResponseEntity<String> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("reportDate") String reportDate
    ) throws IOException {

        File tmp = File.createTempFile("aging", ".xlsx");
        file.transferTo(tmp);

        service.importFile(tmp, LocalDate.parse(reportDate));

        return ResponseEntity.ok("Receivable aging imported");
    }
}
