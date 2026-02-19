package com.example.demo.uploading.service;

import com.example.demo.base.entity.Client;
import com.example.demo.base.entity.ClientStatus;
import com.example.demo.base.entity.RiskAssessment;
import com.example.demo.base.repository.ClientRepository;
import com.example.demo.base.repository.RiskAssessmentRepository;
import com.example.demo.risk.RiskLevel;
import com.example.demo.uploading.archive.ArchiveReason;
import com.example.demo.uploading.archive.ArchiveService;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ClientExcelUploadService {

    private final ClientRepository clientRepository;
    private final RiskAssessmentRepository riskRepository;
    private final ArchiveService archiveService;

    public ClientExcelUploadService(ClientRepository clientRepository,
                                    RiskAssessmentRepository riskRepository,ArchiveService archiveService) {
        this.clientRepository = clientRepository;
        this.riskRepository = riskRepository;
        this.archiveService = archiveService;
    }
    @Transactional
    public void upload(InputStream inputStream,
                       String clientId,
                       String clientName,
                       boolean replaceConfirmed) throws Exception, ClientAlreadyExistsException {

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() < 2)
                throw new RuntimeException("Excel file is empty");

            Map<String, Integer> columns = resolveColumns(sheet.getRow(0));
            Integer amountCol = findColumn(columns, "amount", "value", "total", "exposure", "kwota", "saldo");
            Integer daysCol = findColumn(columns, "days overdue", "days", "overdue_days", "dni po terminie");

            if (amountCol == null)
                throw new RuntimeException("Missing required column: amount");

            Optional<Client> existingOpt = clientRepository.findByExternalId(clientId);
            if (existingOpt.isPresent()) {
                if (!replaceConfirmed) {
                    throw new ClientAlreadyExistsException(clientId);
                }
                archiveService.archiveClientWithInvoices(existingOpt.get(), ArchiveReason.REPLACED_BY_UPLOAD);
            }

            Client client = existingOpt.orElse(Client.builder(clientId, clientName, ClientStatus.ACTIVE)
                    .creditLimit(BigDecimal.ZERO).build());

            client.setFullName(clientName);
            client.setStatus(ClientStatus.ACTIVE);
            clientRepository.save(client);

            BigDecimal totalExposure = BigDecimal.ZERO;
            int maxDaysOverdue = 0;
            int overdueInvoices = 0;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                BigDecimal amount = getCellDecimal(row, amountCol);
                Integer daysOverdue = getCellInteger(row, daysCol);
                if (amount == null) continue;

                totalExposure = totalExposure.add(amount);
                if (daysOverdue != null && daysOverdue > 0) {
                    overdueInvoices++;
                    maxDaysOverdue = Math.max(maxDaysOverdue, daysOverdue);
                }
            }

            int riskScore = calculateRiskScore(totalExposure, maxDaysOverdue, overdueInvoices);
            RiskLevel riskLevel = mapRiskLevel(riskScore);

            RiskAssessment risk = new RiskAssessment.Builder(client, riskScore, riskLevel, LocalDateTime.now())
                    .reasons(String.format("Exposure=%s, MaxDaysOverdue=%d, OverdueInvoices=%d",
                            totalExposure, maxDaysOverdue, overdueInvoices))
                    .recommendation(buildRecommendation(riskLevel))
                    .build();

            riskRepository.save(risk);
        }
    }



    private int calculateRiskScore(BigDecimal exposure,
                                   int maxDaysOverdue,
                                   int overdueInvoices) {

        int score = 0;

        if (exposure.compareTo(new BigDecimal("100000")) > 0) {
            score += 40;
        }

        if (maxDaysOverdue > 90) {
            score += 40;
        } else if (maxDaysOverdue > 30) {
            score += 20;
        }

        if (overdueInvoices > 5) {
            score += 20;
        }

        return Math.min(score, 100);
    }

    private RiskLevel mapRiskLevel(int score) {
        if (score >= 70) return RiskLevel.HIGH;
        if (score >= 40) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    private String buildReasons(BigDecimal exposure,
                                int maxDaysOverdue,
                                int overdueInvoices) {

        return "Exposure=" + exposure +
                ", MaxDaysOverdue=" + maxDaysOverdue +
                ", OverdueInvoices=" + overdueInvoices;
    }

    private String buildRecommendation(RiskLevel level) {
        return switch (level) {
            case HIGH -> "Immediate review required. Consider credit limit reduction.";
            case MEDIUM -> "Monitor payment behaviour closely.";
            case LOW -> "Standard monitoring.";
            case CRITICAL -> "immidiatly work";
        };
    }


    private Map<String, Integer> resolveColumns(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();

        for (Cell cell : headerRow) {
            if (cell.getCellType() != CellType.STRING) continue;

            String header = normalize(cell.getStringCellValue());
            map.put(header, cell.getColumnIndex());
        }

        return map;
    }

    private String normalize(String value) {
        if (value == null) return null;

        return value
                .toLowerCase()
                .trim()
                .replace("ą", "a")
                .replace("ć", "c")
                .replace("ę", "e")
                .replace("ł", "l")
                .replace("ń", "n")
                .replace("ó", "o")
                .replace("ś", "s")
                .replace("ż", "z")
                .replace("ź", "z");
    }

    private Integer findColumn(Map<String, Integer> map, String... names) {
        for (String name : names) {
            String normalized = normalize(name);
            Integer index = map.get(normalized);
            if (index != null) {
                return index;
            }
        }
        return null;
    }


    private BigDecimal getCellDecimal(Row row, Integer index) {
        if (index == null) return null;

        Cell cell = row.getCell(index);
        if (cell == null) return null;

        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
                case STRING -> new BigDecimal(cell.getStringCellValue());
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    private Integer getCellInteger(Row row, Integer index) {
        if (index == null) return null;

        Cell cell = row.getCell(index);
        if (cell == null) return null;

        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> (int) cell.getNumericCellValue();
                case STRING -> Integer.parseInt(cell.getStringCellValue());
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

}
