package com.example.demo.excelUpload.controller;

import com.example.demo.excelUpload.service.BisnodeExcelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/import")
public class ImportController {

    private final BisnodeExcelService bisnodeService;

    public ImportController(BisnodeExcelService bisnodeService) {
        this.bisnodeService = bisnodeService;
    }

    @PostMapping("/bisnode")
    public ResponseEntity<String> importBisnode(@RequestParam("file") MultipartFile file) throws IOException, IOException {
        File tempFile = File.createTempFile("bisnode", ".xlsx");
        file.transferTo(tempFile);

        bisnodeService.importBisnodeFile(tempFile);
        return ResponseEntity.ok("Imported successfully");
    }

}
