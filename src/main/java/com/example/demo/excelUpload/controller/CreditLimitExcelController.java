package com.example.demo.excelUpload.controller;

import com.example.demo.excelUpload.service.CreditLimitExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/credit-limits")
public class CreditLimitExcelController {

    private final CreditLimitExcelService service;

    public CreditLimitExcelController(CreditLimitExcelService service) {
        this.service = service;
    }

    @PostMapping("/import")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file)
            throws IOException {

        File tempFile = File.createTempFile("credit-limit-", ".xlsx");
        file.transferTo(tempFile);

        service.importFile(tempFile);

        return ResponseEntity.ok("Credit limits imported");
    }
}
