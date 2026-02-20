package com.example.demo.base.base.service;

import com.example.demo.base.base.dto.ClientDashboardRow;
import com.example.demo.base.base.entity.Client;
import com.example.demo.base.base.entity.ClientStatus;
import com.example.demo.base.base.entity.RiskAssessment;
import com.example.demo.base.base.repository.ClientDashboardRepository;
import com.example.demo.base.base.repository.ClientRepository;
import com.example.demo.base.risk.RiskLevel;
import com.opencsv.CSVReader;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.demo.base.base.entity.ClientStatus.ACTIVE;

@Service
public class ClientService {
    private final ClientRepository clientRepository;
    private final ClientDashboardRepository dashboardRepository;

    public ClientService(ClientRepository clientRepository, ClientDashboardRepository clientDashBoardRepository) {
        this.clientRepository = clientRepository;
        this.dashboardRepository = clientDashBoardRepository;
    }
    public Client createClient(String externalId, String fullName, ClientStatus status) {
        Client client = new Client.Builder(externalId, fullName, status).build();
        return clientRepository.save(client);
    }

    public List<Client> searchByName(String name) {
        return clientRepository.findByFullNameContainingIgnoreCase(name);
    }

    public Client getByExternalId(String externalId) {
        return clientRepository.findByExternalId(externalId.trim())
                .orElseThrow(() ->
                        new IllegalStateException("Client not found: " + externalId));
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
    public List<ClientDashboardRow> getDashboardItems(Sort sort) {
        List<ClientDashboardRow> dashboard = dashboardRepository.fetchDashboardRaw().stream()
                .map(row -> {
                    Client client = (Client) row[0];
                    RiskAssessment risk = (RiskAssessment) row[1];
                    RiskLevel level = risk != null ? risk.getRiskLevel() : null;
                    return new ClientDashboardRow(client, level);
                })
                .collect(Collectors.toList());

        Sort.Order order = sort.stream().findFirst().orElse(Sort.Order.asc("id"));

        Comparator<ClientDashboardRow> comparator = switch (order.getProperty()) {
            case "externalId" -> Comparator.comparing(row -> row.getClient().getExternalId());
            case "name" -> Comparator.comparing(row -> row.getClient().getName(), String.CASE_INSENSITIVE_ORDER);
            case "risk" -> Comparator.comparing(
                    row -> row.getRiskLevel() != null ? row.getRiskLevel().getPriority() : 0
            );
            default -> Comparator.comparing(row -> row.getClient().getId());
        };
        if (order.getDirection() == Sort.Direction.DESC) {
            comparator = comparator.reversed();
        }

        dashboard.sort(comparator);

        return dashboard;
    }

    public Page<Client> findAll(Pageable pageable) {
        return clientRepository.findAll(pageable);
    }
}