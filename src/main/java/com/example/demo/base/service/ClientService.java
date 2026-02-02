package com.example.demo.base.service;

import com.example.demo.base.entity.Client;
import com.example.demo.base.entity.ClientStatus;
import com.example.demo.base.repository.ClientRepository;
import com.opencsv.CSVReader;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static com.example.demo.base.entity.ClientStatus.ACTIVE;

@Service
public class ClientService {
    private final ClientRepository clientRepository;

    ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }
    public Client createClient(String externalId, String fullName, ClientStatus status) {
        Client client = new Client.Builder(externalId, fullName, status).build();
        return clientRepository.save(client);
    }

    public List<Client> searchByName(String name) {
        return clientRepository.findByFullNameContainingIgnoreCase(name);
    }

    public Client getByExternalId(String externalId) {
        return clientRepository.findByExternalId(externalId)
                .orElseThrow(() -> new IllegalStateException("Client not found"));
    }
    public Client getClient(Long id) {
    return clientRepository.findById(id).orElseThrow(() -> new IllegalStateException("Client not found"));
    }
    public void importFromCsv(MultipartFile file) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] line;
            reader.readNext(); // skip header
            while ((line = reader.readNext()) != null) {
                String nazwa = line[0];
                String dax = line[1];
                String bisnode = line[2];

                Client client = Client.builder(
                                dax,         // externalId
                                nazwa,       // fullName
                                ACTIVE // default status
                        )
                        .ratingExternal(bisnode)
                        .build();

                clientRepository.save(client);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void importFromExcel(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {

            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {

                if (row.getRowNum() == 0) continue; // header

                String nazwa = getCellValue(row.getCell(0));
                String nrOdb = getCellValue(row.getCell(1));
                String bisnode = getCellValue(row.getCell(2));

                if (nrOdb == null || nrOdb.isBlank()) continue;

                Client client = clientRepository
                        .findByExternalId(nrOdb)
                        .orElseGet(Client::new);

                client.setExternalId(nrOdb);
                client.setFullName(nazwa);
                client.setRatingExternal(bisnode);
                client.setStatus(ACTIVE);

                clientRepository.save(client);
            }

        } catch (Exception e) {
            throw new RuntimeException("Błąd importu Excel", e);
        }
    }


    private String getCellValue(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    public List<Client> getAll() {
        return clientRepository.findAll();
    }
}
