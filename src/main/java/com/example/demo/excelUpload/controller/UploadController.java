package com.example.demo.base.excelUpload.controller;

import com.example.demo.base.base.service.ClientService;
import com.example.demo.base.excelUpload.service.BisnodeExcelService;
import com.example.demo.base.excelUpload.service.CreditLimitExcelService;
import com.example.demo.base.excelUpload.service.ReceivableAgingExcelService;
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

//    @PostMapping("/upload-file")
//    public String handleUpload(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam("fileType") String fileType,
//            @RequestParam(value = "reportDate", required = false) String reportDate,
//            RedirectAttributes redirectAttributes
//    ) {
//
//        try {
//
//            switch (fileType) {
//
//                case "bisnode" -> {
//                    File tempFile = File.createTempFile("bisnode", ".xlsx");
//                    file.transferTo(tempFile);
//                    bisnodeService.importBisnodeFile(tempFile);
//                }
//
//                case "receivable-aging" -> {
//
//                    if (reportDate == null || reportDate.isBlank()) {
//                        throw new IllegalArgumentException("Report date is required");
//                    }
//
//                    File tmp = File.createTempFile("aging", ".xlsx");
//                    file.transferTo(tmp);
//
//                    receivableService.importFile(tmp, LocalDate.parse(reportDate));
//                }
//
//
//                case "credit-limits" -> {
//                    File tempFile = File.createTempFile("limits-", ".xlsx");
//                    file.transferTo(tempFile);
//                    creditLimitService.importFile(tempFile);
//                }
//
//                case "bisnode-agg" -> {
//
//                    clientService.importFromExcel(file);
//                }
//
//                default -> throw new IllegalArgumentException("Unknown file type");
//            }
//
//            redirectAttributes.addFlashAttribute("message", "Import successful!");
//
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("message", "Error: " + e.getMessage());
//        }
//
//        return "redirect:/upload-file";
//    }
@PostMapping("/upload-file")
public String handleUpload(
        @RequestParam("file") MultipartFile file,
        @RequestParam("fileType") String fileType,
        @RequestParam(value = "reportDate", required = false) String reportDate,
        RedirectAttributes redirectAttributes
) {

    System.out.println("=== UPLOAD START ===");
    System.out.println("File name: " + file.getOriginalFilename());
    System.out.println("File type: " + fileType);
    System.out.println("Report date: " + reportDate);

    try {

        switch (fileType) {

            case "bisnode" -> {
                System.out.println("Processing BISNODE...");
                File tempFile = File.createTempFile("bisnode", ".xlsx");
                file.transferTo(tempFile);
                bisnodeService.importBisnodeFile(tempFile);
            }

            case "receivable-aging" -> {
                System.out.println("Processing RECEIVABLE AGING...");

                if (reportDate == null || reportDate.isBlank()) {
                    throw new IllegalArgumentException("Report date is required");
                }

                File tmp = File.createTempFile("aging", ".xlsx");
                file.transferTo(tmp);

                receivableService.importFile(tmp, LocalDate.parse(reportDate));
            }

            case "credit-limits" -> {
                System.out.println("Processing CREDIT LIMITS...");
                File tempFile = File.createTempFile("limits-", ".xlsx");
                file.transferTo(tempFile);
                creditLimitService.importFile(tempFile);
            }

            case "bisnode-agg" -> {
                System.out.println("Processing BISNODE AGG...");
                clientService.importFromExcel(file);
            }

            default -> {
                System.out.println("UNKNOWN FILE TYPE!");
                throw new IllegalArgumentException("Unknown file type");
            }
        }

        System.out.println("=== IMPORT SUCCESS ===");
        redirectAttributes.addFlashAttribute("message", "Import successful!");

    } catch (Exception e) {

        System.out.println("=== IMPORT ERROR ===");
        e.printStackTrace();

        redirectAttributes.addFlashAttribute("message", "Error: " + e.getMessage());
    }

    return "redirect:/upload-file";
}
}
