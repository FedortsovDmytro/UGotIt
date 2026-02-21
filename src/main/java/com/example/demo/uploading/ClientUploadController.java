package com.example.demo.uploading;

import com.example.demo.base.dto.UploadResult;
import com.example.demo.uploading.dto.ReceivableRecord;
import com.example.demo.uploading.dto.ReceivableAgingBuilder;
import com.example.demo.risk.AgingResult;
import com.example.demo.uploading.service.ClientAlreadyExistsException;
import com.example.demo.uploading.service.ClientExcelUploadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.poi.ss.usermodel.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;

@Controller
@RequestMapping("/clients")
public class ClientUploadController {

    private final ClientExcelUploadService uploadService;

    public ClientUploadController(ClientExcelUploadService uploadService) {
        this.uploadService = uploadService;
    }

    @GetMapping("/upload")
    public String showUploadPage() {
        return "clients/upload";
    }

@PostMapping("/upload")
public String uploadClients(
        @RequestParam("file") MultipartFile file,
        @RequestParam("clientId") String clientId,
        @RequestParam("clientName") String clientName,
        @RequestParam(value = "replace", defaultValue = "false") boolean replaceConfirmed,
        Model model,
        HttpSession session,
        HttpServletRequest request) {

    try {
        session.setAttribute("pendingFile", file.getBytes());


        UploadResult result = uploadService.uploadAndReturnResult(
                new ByteArrayInputStream(file.getBytes()),
                clientId,
                clientName,
                replaceConfirmed,
                request.getLocale()
        );

        session.removeAttribute("pendingFile");

        model.addAttribute("result", result);
        model.addAttribute("success", "Client uploaded successfully!");
        return "clients/upload";

    } catch (ClientAlreadyExistsException ex) {
        model.addAttribute("clientId", clientId);
        model.addAttribute("clientName", clientName);
        return "clients/confirm-replace";
    } catch (Exception ex) {
        ex.printStackTrace();
        model.addAttribute("error", "Upload failed: " + ex.getMessage());
        return "clients/upload";
    }
}

 @PostMapping ("/upload/replace")
    public String replaceClient(
            @RequestParam("clientId") String clientId,
            @RequestParam("clientName") String clientName,
            HttpSession session,
            Model model,
            HttpServletRequest request) {

        byte[] fileBytes = (byte[]) session.getAttribute("pendingFile");

        if (fileBytes == null || fileBytes.length == 0) {
            model.addAttribute("error", "Original file not found in session. Please upload again.");
            return "clients/upload";
        }

        try {
            UploadResult result = uploadService.uploadAndReturnResult(
                    new ByteArrayInputStream(fileBytes),
                    clientId,
                    clientName,
                    true,
                    request.getLocale()
            );

            session.removeAttribute("pendingFile");
            model.addAttribute("result", result);
            model.addAttribute("success", "Client replaced successfully!");
            return "clients/upload";

        } catch (Exception ex) {
            ex.printStackTrace();
            model.addAttribute("error", "Failed to replace client: " + ex.getMessage());
            return "clients/upload";
        } catch (ClientAlreadyExistsException e) {
            throw new RuntimeException(e);
        }
    }
}
