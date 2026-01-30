package com.example.demo.excelUpload.controller;

import com.example.demo.excelUpload.servise.CreditLimitExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Controller
@RequestMapping("/import")
public class CreditLimitController {
    private final CreditLimitExcelService creditLimitService;
    @Autowired
    public CreditLimitController(CreditLimitExcelService creditLimitService) {
        this.creditLimitService = creditLimitService;
    }
    @PostMapping("/credit-limit")
    public ResponseEntity<String> upload(@RequestParam MultipartFile file) throws IOException {
        File tmp = File.createTempFile("cl", ".xlsx");
        file.transferTo(tmp);
        creditLimitService.importFile(tmp);
        return ResponseEntity.ok("Credit limits imported");
    }

}
