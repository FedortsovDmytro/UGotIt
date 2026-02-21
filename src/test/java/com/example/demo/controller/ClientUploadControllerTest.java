package com.example.demo.controller;


import com.example.demo.base.dto.UploadResult;
import com.example.demo.uploading.ClientUploadController;
import com.example.demo.uploading.service.ClientAlreadyExistsException;
import com.example.demo.uploading.service.ClientExcelUploadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ClientUploadControllerTest {

    private ClientExcelUploadService uploadService;
    private ClientUploadController controller;
    private Model model;
    private HttpSession session;
    private HttpServletRequest request;
    private MultipartFile file;

    @BeforeEach
    void setup() {
        uploadService = mock(ClientExcelUploadService.class);
        controller = new ClientUploadController(uploadService);
        model = mock(Model.class);
        session = mock(HttpSession.class);
        request = mock(HttpServletRequest.class);
        file = mock(MultipartFile.class);
    }

    @Test
    void testShowUploadPage() {
        String view = controller.showUploadPage();
        assertEquals("clients/upload", view);
    }

    @Test
    void testUploadClients_success() throws Exception {
        byte[] content = "test".getBytes();
        when(file.getBytes()).thenReturn(content);
        try {
            when(uploadService.uploadAndReturnResult(any(ByteArrayInputStream.class), eq("123"), eq("ABC"), eq(false), any(Locale.class)))
                    .thenReturn(new UploadResult());
        } catch (ClientAlreadyExistsException e) {
            throw new RuntimeException(e);
        }

        String view = controller.uploadClients(file, "123", "ABC", false, model, session, request);
        assertEquals("clients/upload", view);
        verify(session).removeAttribute("pendingFile");
        verify(model).addAttribute(eq("success"), anyString());
    }


    @Test
    void testReplaceClient_success() throws Exception {
        byte[] fileBytes = "test".getBytes();
        when(session.getAttribute("pendingFile")).thenReturn(fileBytes);
        try {
            when(uploadService.uploadAndReturnResult(any(ByteArrayInputStream.class), eq("123"), eq("ABC"), eq(true), any(Locale.class)))
                    .thenReturn(new UploadResult());
        } catch (ClientAlreadyExistsException e) {
            throw new RuntimeException(e);
        }

        String view = controller.replaceClient("123", "ABC", session, model, request);
        assertEquals("clients/upload", view);
        verify(session).removeAttribute("pendingFile");
        verify(model).addAttribute(eq("success"), anyString());
    }

    @Test
    void testReplaceClient_noFile() {
        when(session.getAttribute("pendingFile")).thenReturn(null);

        String view = controller.replaceClient("123", "ABC", session, model, request);
        assertEquals("clients/upload", view);
        verify(model).addAttribute(eq("error"), anyString());
    }
}