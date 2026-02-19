package com.example.demo.excelUpload.controller;

import com.example.demo.base.service.BisnodeService;
import com.example.demo.base.service.ClientService;
import com.example.demo.excelUpload.service.BisnodeExcelService;
import com.example.demo.excelUpload.service.CreditLimitExcelService;
import com.example.demo.excelUpload.service.ReceivableAgingExcelService;
import com.example.demo.uploading.service.ClientExcelUploadService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.time.LocalDate;
@Controller
public class UploadController {

    private final BisnodeExcelService bisnodeService;
    private final ReceivableAgingExcelService receivableService;
    private final CreditLimitExcelService creditLimitService;
    private final ClientService clientService;

    public UploadController(
            BisnodeExcelService bisnodeService,
            ReceivableAgingExcelService receivableService,
            CreditLimitExcelService creditLimitService,
            ClientService clientService
    ) {
        this.bisnodeService = bisnodeService;
        this.receivableService = receivableService;
        this.creditLimitService = creditLimitService;
        this.clientService = clientService;
    }

    @GetMapping("/upload-file")
    public String showForm() {
        return "upload-file";
    }

    @PostMapping("/upload-file")
    public String handleUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileType") String fileType,
            @RequestParam(value = "reportDate", required = false) String reportDate,
            RedirectAttributes redirectAttributes
    ) {

        try {

            switch (fileType) {

                case "bisnode" -> {
                    File tempFile = File.createTempFile("bisnode", ".xlsx");
                    file.transferTo(tempFile);
                    bisnodeService.importBisnodeFile(tempFile);
                }

                case "receivable-aging" -> {

                    if (reportDate == null || reportDate.isBlank()) {
                        throw new IllegalArgumentException("Report date is required");
                    }

                    File tmp = File.createTempFile("aging", ".xlsx");
                    file.transferTo(tmp);

                    receivableService.importFile(tmp, LocalDate.parse(reportDate));
                }


                case "credit-limits" -> {
                    File tempFile = File.createTempFile("limits-", ".xlsx");
                    file.transferTo(tempFile);
                    creditLimitService.importFile(tempFile);
                }

                case "bisnode-agg" -> {

                    clientService.importFromExcel(file);
                }

                default -> throw new IllegalArgumentException("Unknown file type");
            }

            redirectAttributes.addFlashAttribute("message", "Import successful!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error: " + e.getMessage());
        }

        return "redirect:/upload-file";
    }
}
