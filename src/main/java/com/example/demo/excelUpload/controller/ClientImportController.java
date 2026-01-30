package com.example.demo.ekselUploud.controller;

import com.example.demo.entity.Client;
import com.example.demo.service.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientImportController {

    private final ClientService clientService;

    public ClientImportController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping("/import")
    public ResponseEntity<String> importExcel(@RequestParam("file") MultipartFile file) {
        clientService.importFromExcel(file);
        return ResponseEntity.ok("Imported Excel");
    }

    @GetMapping("/all")
    public List<Client> getAll() {
        return clientService.getAll();
    }

}
