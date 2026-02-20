package com.example.demo.base.uploading.service;

import com.example.demo.base.base.dto.UploadResult;
import com.example.demo.base.base.entity.*;
import com.example.demo.base.entity.*;
import com.example.demo.base.base.repository.ClientRepository;
import com.example.demo.base.base.repository.RiskAssessmentRepository;
import com.example.demo.base.excelUpload.repository.CreditLimitRepository;
import com.example.demo.base.excelUpload.repository.ReceivableAgingRepository;
import com.example.demo.base.risk.RiskLevel;
import com.example.demo.base.risk.RiskSignal;
import com.example.demo.base.uploading.archive.ArchiveReason;
import com.example.demo.base.uploading.archive.ArchiveService;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ClientExcelUploadService {

    private final ClientRepository clientRepository;
    private final RiskAssessmentRepository riskRepository;
    private final ArchiveService archiveService;
    private final CreditLimitRepository creditLimitRepository;
    private final ReceivableAgingRepository agingRepository;
    public ClientExcelUploadService(ClientRepository clientRepository,
                                    RiskAssessmentRepository riskRepository,
                                    ArchiveService archiveService,
                                    CreditLimitRepository creditLimitRepository,ReceivableAgingRepository agingRepository) {
        this.clientRepository = clientRepository;
        this.riskRepository = riskRepository;
        this.archiveService = archiveService;
        this.creditLimitRepository = creditLimitRepository;
        this.agingRepository = agingRepository;
    }

    @Transactional
    public UploadResult uploadAndReturnResult(InputStream inputStream,
                                              String clientId,
                                              String clientName,
                                              boolean replaceConfirmed,
                                              Locale locale)
            throws Exception, ClientAlreadyExistsException {

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() < 2)
                throw new RuntimeException("Excel file is empty");

            Map<String, Integer> columns = resolveColumns(sheet.getRow(0));
            Integer amountCol = findColumn(columns, "amount", "value", "total", "exposure", "kwota", "saldo");
            Integer daysCol = findColumn(columns, "days overdue", "days", "overdue_days", "dni po terminie");

            if (amountCol == null)
                throw new RuntimeException("Missing required column: amount");

            // === CLIENT ===
            Optional<Client> existingOpt = clientRepository.findByExternalId(clientId);

            if (existingOpt.isPresent() && !replaceConfirmed) {
                throw new ClientAlreadyExistsException(clientId);
            }

            existingOpt.ifPresent(client ->
                    archiveService.archiveClientWithInvoices(client, ArchiveReason.REPLACED_BY_UPLOAD)
            );

            Client client = existingOpt.orElse(
                    Client.builder(clientId, clientName, ClientStatus.ACTIVE)
                            .creditLimit(BigDecimal.ZERO)
                            .build()
            );

            client.setFullName(clientName);
            client.setStatus(ClientStatus.ACTIVE);
            client = clientRepository.save(client);

            // === CALCULATIONS ===
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
            RiskLevel riskLevelEnum = mapRiskLevel(riskScore);

            String reasons = buildReasons(totalExposure, maxDaysOverdue, overdueInvoices, locale);
            String recommendation = buildRecommendation(riskLevelEnum);
            LocalDateTime calculatedAt = LocalDateTime.now();

            // === SAVE RISK ASSESSMENT ===
            RiskAssessment risk = new RiskAssessment.Builder(client, riskScore, riskLevelEnum, calculatedAt)
                    .reasons(reasons)
                    .recommendation(recommendation)
                    .build();

            riskRepository.save(risk);


            CreditLimit creditLimit = new CreditLimit();
            creditLimit.setClient(client);
            creditLimit.setLimitAmount(client.getCreditLimit() != null ? client.getCreditLimit() : BigDecimal.ZERO);
            creditLimit.setUsedAmount(totalExposure);
            creditLimit.setPaymentTermsDays(maxDaysOverdue);
            creditLimit.setValidFrom(LocalDate.now());
            creditLimitRepository.save(creditLimit);

            // === SAVE RECEIVABLE AGING ===
            ReceivableAging aging = new ReceivableAging();
            aging.setClient(client);
            aging.setReportDate(LocalDate.now());
            aging.setTotalAmount(totalExposure);
            aging.setPaymentTermsDays(maxDaysOverdue);

            aging.setNotDue(BigDecimal.ZERO);
            aging.setOverdue1to7(BigDecimal.ZERO);
            aging.setOverdue8to14(BigDecimal.ZERO);
            aging.setOverdue15to30(BigDecimal.ZERO);
            aging.setOverdue31to60(BigDecimal.ZERO);
            aging.setOverdue61to90(BigDecimal.ZERO);
            aging.setOverdue91to120(BigDecimal.ZERO);
            aging.setOverdue121to360(BigDecimal.ZERO);
            aging.setOverdueAbove360(BigDecimal.ZERO);
            aging.setLegalEvents(String.valueOf(BigDecimal.ZERO));
            aging.setCalculatedAt(LocalDateTime.now());
            agingRepository.save(aging);

            // === BUILD RESULT DTO ===
            UploadResult result = new UploadResult();
            result.setClientId(client.getExternalId());
            result.setClientName(client.getName());
            result.setCreditLimit(creditLimit.getLimitAmount());
            result.setExposure(totalExposure);
            result.setAverageDaysPastDue(overdueInvoices > 0 ? maxDaysOverdue : null);
            result.setRiskScore(riskScore);
            result.setRiskLevel(riskLevelEnum.name());
            result.setReasons(reasons);
            result.setRecommendation(recommendation);
            result.setCalculationDate(calculatedAt);

            return result;
        }
    }

    // -------------------- Risk helpers --------------------

    private int calculateRiskScore(BigDecimal exposure, int maxDaysOverdue, int overdueInvoices) {
        int score = 0;
        if (exposure.compareTo(new BigDecimal("100000")) > 0) score += 40;
        if (maxDaysOverdue > 90) score += 40;
        else if (maxDaysOverdue > 30) score += 20;
        if (overdueInvoices > 5) score += 20;
        return Math.min(score, 100);
    }

    private RiskLevel mapRiskLevel(int score) {
        if (score >= 70) return RiskLevel.HIGH;
        if (score >= 40) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    private String buildReasons(BigDecimal exposure, int maxDaysOverdue, int overdueInvoices, Locale locale) {
        List<RiskSignal> signals = new ArrayList<>();
        if (maxDaysOverdue >= 1 && maxDaysOverdue <= 7) signals.add(RiskSignal.OVERDUE_1_7);
        else if (maxDaysOverdue >= 8 && maxDaysOverdue <= 14) signals.add(RiskSignal.OVERDUE_8_14);
        else if (maxDaysOverdue >= 15 && maxDaysOverdue <= 30) signals.add(RiskSignal.OVERDUE_15_30);
        else if (maxDaysOverdue >= 31 && maxDaysOverdue <= 60) signals.add(RiskSignal.OVERDUE_31_60);
        else if (maxDaysOverdue > 60) signals.add(RiskSignal.OVERDUE_60_PLUS);

        if (overdueInvoices == 0 && exposure.compareTo(BigDecimal.ZERO) > 0)
            signals.add(RiskSignal.NEW_CLIENT);

        return signals.stream()
                .map(s -> s.getDescription(locale))
                .reduce((a, b) -> a + "; " + b)
                .orElse(String.format("Exposure=%s, MaxDaysOverdue=%d, OverdueInvoices=%d",
                        exposure, maxDaysOverdue, overdueInvoices));
    }

    private String buildRecommendation(RiskLevel level) {
        return switch (level) {
            case HIGH -> "Immediate review required. Consider credit limit reduction.";
            case MEDIUM -> "Monitor payment behaviour closely.";
            case LOW -> "Standard monitoring.";
            case CRITICAL -> "Immediate action required.";
        };
    }

    // -------------------- Excel helpers --------------------

    private Map<String, Integer> resolveColumns(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        for (Cell cell : headerRow) {
            if (cell.getCellType() != CellType.STRING) continue;
            map.put(normalize(cell.getStringCellValue()), cell.getColumnIndex());
        }
        return map;
    }

    private String normalize(String value) {
        if (value == null) return null;
        return value.toLowerCase().trim()
                .replace("ą", "a").replace("ć", "c").replace("ę", "e")
                .replace("ł", "l").replace("ń", "n").replace("ó", "o")
                .replace("ś", "s").replace("ż", "z").replace("ź", "z");
    }

    private Integer findColumn(Map<String, Integer> map, String... names) {
        for (String name : names) {
            Integer index = map.get(normalize(name));
            if (index != null) return index;
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
//package com.example.demo.uploading.service;

//
//import com.example.demo.base.entity.Client;
//import com.example.demo.base.entity.ClientStatus;
//import com.example.demo.base.entity.RiskAssessment;
//import com.example.demo.base.repository.ClientRepository;
//import com.example.demo.base.repository.RiskAssessmentRepository;
//import com.example.demo.risk.RiskLevel;
//import com.example.demo.uploading.archive.ArchiveReason;
//import com.example.demo.uploading.archive.ArchiveService;
//import org.apache.poi.ss.usermodel.*;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.InputStream;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Optional;
//
//@Service
//public class ClientExcelUploadService {
//
//    private final ClientRepository clientRepository;
//    private final RiskAssessmentRepository riskRepository;
//    private final ArchiveService archiveService;
//
//    public ClientExcelUploadService(ClientRepository clientRepository,
//                                    RiskAssessmentRepository riskRepository,ArchiveService archiveService) {
//        this.clientRepository = clientRepository;
//        this.riskRepository = riskRepository;
//        this.archiveService = archiveService;
//    }
//    @Transactional
//    public void upload(InputStream inputStream,
//                       String clientId,
//                       String clientName,
//                       boolean replaceConfirmed) throws Exception, ClientAlreadyExistsException {
//
//        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
//            Sheet sheet = workbook.getSheetAt(0);
//            if (sheet.getPhysicalNumberOfRows() < 2)
//                throw new RuntimeException("Excel file is empty");
//
//            Map<String, Integer> columns = resolveColumns(sheet.getRow(0));
//            Integer amountCol = findColumn(columns, "amount", "value", "total", "exposure", "kwota", "saldo");
//            Integer daysCol = findColumn(columns, "days overdue", "days", "overdue_days", "dni po terminie");
//
//            if (amountCol == null)
//                throw new RuntimeException("Missing required column: amount");
//
//            Optional<Client> existingOpt = clientRepository.findByExternalId(clientId);
//            if (existingOpt.isPresent()) {
//                if (!replaceConfirmed) {
//                    throw new ClientAlreadyExistsException(clientId);
//                }
//                archiveService.archiveClientWithInvoices(existingOpt.get(), ArchiveReason.REPLACED_BY_UPLOAD);
//            }
//
//            Client client = existingOpt.orElse(Client.builder(clientId, clientName, ClientStatus.ACTIVE)
//                    .creditLimit(BigDecimal.ZERO).build());
//
//            client.setFullName(clientName);
//            client.setStatus(ClientStatus.ACTIVE);
//            clientRepository.save(client);
//
//            BigDecimal totalExposure = BigDecimal.ZERO;
//            int maxDaysOverdue = 0;
//            int overdueInvoices = 0;
//
//            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
//                Row row = sheet.getRow(i);
//                if (row == null) continue;
//
//                BigDecimal amount = getCellDecimal(row, amountCol);
//                Integer daysOverdue = getCellInteger(row, daysCol);
//                if (amount == null) continue;
//
//                totalExposure = totalExposure.add(amount);
//                if (daysOverdue != null && daysOverdue > 0) {
//                    overdueInvoices++;
//                    maxDaysOverdue = Math.max(maxDaysOverdue, daysOverdue);
//                }
//            }
//
//            int riskScore = calculateRiskScore(totalExposure, maxDaysOverdue, overdueInvoices);
//            RiskLevel riskLevel = mapRiskLevel(riskScore);
//
//            RiskAssessment risk = new RiskAssessment.Builder(client, riskScore, riskLevel, LocalDateTime.now())
//                    .reasons(String.format("Exposure=%s, MaxDaysOverdue=%d, OverdueInvoices=%d",
//                            totalExposure, maxDaysOverdue, overdueInvoices))
//                    .recommendation(buildRecommendation(riskLevel))
//                    .build();
//
//            riskRepository.save(risk);
//        }
//    }
//
//
//
//    private int calculateRiskScore(BigDecimal exposure,
//                                   int maxDaysOverdue,
//                                   int overdueInvoices) {
//
//        int score = 0;
//
//        if (exposure.compareTo(new BigDecimal("100000")) > 0) {
//            score += 40;
//        }
//
//        if (maxDaysOverdue > 90) {
//            score += 40;
//        } else if (maxDaysOverdue > 30) {
//            score += 20;
//        }
//
//        if (overdueInvoices > 5) {
//            score += 20;
//        }
//
//        return Math.min(score, 100);
//    }
//
//    private RiskLevel mapRiskLevel(int score) {
//        if (score >= 70) return RiskLevel.HIGH;
//        if (score >= 40) return RiskLevel.MEDIUM;
//        return RiskLevel.LOW;
//    }
//
//    private String buildReasons(BigDecimal exposure,
//                                int maxDaysOverdue,
//                                int overdueInvoices) {
//
//        return "Exposure=" + exposure +
//                ", MaxDaysOverdue=" + maxDaysOverdue +
//                ", OverdueInvoices=" + overdueInvoices;
//    }
//
//    private String buildRecommendation(RiskLevel level) {
//        return switch (level) {
//            case HIGH -> "Immediate review required. Consider credit limit reduction.";
//            case MEDIUM -> "Monitor payment behaviour closely.";
//            case LOW -> "Standard monitoring.";
//            case CRITICAL -> "immidiatly work";
//        };
//    }
//
//
//    private Map<String, Integer> resolveColumns(Row headerRow) {
//        Map<String, Integer> map = new HashMap<>();
//
//        for (Cell cell : headerRow) {
//            if (cell.getCellType() != CellType.STRING) continue;
//
//            String header = normalize(cell.getStringCellValue());
//            map.put(header, cell.getColumnIndex());
//        }
//
//        return map;
//    }
//
//    private String normalize(String value) {
//        if (value == null) return null;
//
//        return value
//                .toLowerCase()
//                .trim()
//                .replace("ą", "a")
//                .replace("ć", "c")
//                .replace("ę", "e")
//                .replace("ł", "l")
//                .replace("ń", "n")
//                .replace("ó", "o")
//                .replace("ś", "s")
//                .replace("ż", "z")
//                .replace("ź", "z");
//    }
//
//    private Integer findColumn(Map<String, Integer> map, String... names) {
//        for (String name : names) {
//            String normalized = normalize(name);
//            Integer index = map.get(normalized);
//            if (index != null) {
//                return index;
//            }
//        }
//        return null;
//    }
//
//
//    private BigDecimal getCellDecimal(Row row, Integer index) {
//        if (index == null) return null;
//
//        Cell cell = row.getCell(index);
//        if (cell == null) return null;
//
//        try {
//            return switch (cell.getCellType()) {
//                case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
//                case STRING -> new BigDecimal(cell.getStringCellValue());
//                default -> null;
//            };
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    private Integer getCellInteger(Row row, Integer index) {
//        if (index == null) return null;
//
//        Cell cell = row.getCell(index);
//        if (cell == null) return null;
//
//        try {
//            return switch (cell.getCellType()) {
//                case NUMERIC -> (int) cell.getNumericCellValue();
//                case STRING -> Integer.parseInt(cell.getStringCellValue());
//                default -> null;
//            };
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//}
//package com.example.demo.uploading.service;
//
//
//import com.example.demo.base.entity.Client;
//import com.example.demo.base.entity.ClientStatus;
//import com.example.demo.base.entity.RiskAssessment;
//import com.example.demo.base.repository.ClientRepository;
//import com.example.demo.base.repository.RiskAssessmentRepository;
//import com.example.demo.risk.RiskLevel;
//import com.example.demo.risk.RiskSignal;
//import com.example.demo.uploading.archive.ArchiveReason;
//import com.example.demo.uploading.archive.ArchiveService;
//import com.example.demo.uploading.service.ClientAlreadyExistsException;
//import org.apache.poi.ss.usermodel.*;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.InputStream;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.*;
//
//@Service
//public class ClientExcelUploadService {
//
//    private final ClientRepository clientRepository;
//    private final RiskAssessmentRepository riskRepository;
//    private final ArchiveService archiveService;
//
//    public ClientExcelUploadService(ClientRepository clientRepository,
//                                    RiskAssessmentRepository riskRepository,ArchiveService archiveService) {
//        this.clientRepository = clientRepository;
//        this.riskRepository = riskRepository;
//        this.archiveService = archiveService;
//    }
//    @Transactional
//    public void upload(InputStream inputStream,
//                       String clientId,
//                       String clientName,
//                       boolean replaceConfirmed) throws Exception, ClientAlreadyExistsException {
//
//        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
//            Sheet sheet = workbook.getSheetAt(0);
//            if (sheet.getPhysicalNumberOfRows() < 2)
//                throw new RuntimeException("Excel file is empty");
//
//            Map<String, Integer> columns = resolveColumns(sheet.getRow(0));
//            Integer amountCol = findColumn(columns, "amount", "value", "total", "exposure", "kwota", "saldo");
//            Integer daysCol = findColumn(columns, "days overdue", "days", "overdue_days", "dni po terminie");
//
//            if (amountCol == null)
//                throw new RuntimeException("Missing required column: amount");
//
//            Optional<Client> existingOpt = clientRepository.findByExternalId(clientId);
//            if (existingOpt.isPresent()) {
//                if (!replaceConfirmed) {
//                    throw new ClientAlreadyExistsException(clientId);
//                }
//                archiveService.archiveClientWithInvoices(existingOpt.get(), ArchiveReason.REPLACED_BY_UPLOAD);
//            }
//
//            Client client = existingOpt.orElse(Client.builder(clientId, clientName, ClientStatus.ACTIVE)
//                    .creditLimit(BigDecimal.ZERO).build());
//
//            client.setFullName(clientName);
//            client.setStatus(ClientStatus.ACTIVE);
//            clientRepository.save(client);
//
//            BigDecimal totalExposure = BigDecimal.ZERO;
//            int maxDaysOverdue = 0;
//            int overdueInvoices = 0;
//
//            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
//                Row row = sheet.getRow(i);
//                if (row == null) continue;
//
//                BigDecimal amount = getCellDecimal(row, amountCol);
//                Integer daysOverdue = getCellInteger(row, daysCol);
//                if (amount == null) continue;
//
//                totalExposure = totalExposure.add(amount);
//                if (daysOverdue != null && daysOverdue > 0) {
//                    overdueInvoices++;
//                    maxDaysOverdue = Math.max(maxDaysOverdue, daysOverdue);
//                }
//            }
//
//            int riskScore = calculateRiskScore(totalExposure, maxDaysOverdue, overdueInvoices);
//            RiskLevel riskLevel = mapRiskLevel(riskScore);
//
//            Locale locale = Locale.getDefault();
//
//            RiskAssessment risk = new RiskAssessment.Builder(client, riskScore, riskLevel, LocalDateTime.now())
//                    .reasons(buildReasons(totalExposure, maxDaysOverdue, overdueInvoices, locale))
//                    .recommendation(buildRecommendation(riskLevel))
//                    .build();
//
//            riskRepository.save(risk);
//        }
//    }
//
//
//
//    private int calculateRiskScore(BigDecimal exposure,
//                                   int maxDaysOverdue,
//                                   int overdueInvoices) {
//
//        int score = 0;
//
//        if (exposure.compareTo(new BigDecimal("100000")) > 0) {
//            score += 40;
//        }
//
//        if (maxDaysOverdue > 90) {
//            score += 40;
//        } else if (maxDaysOverdue > 30) {
//            score += 20;
//        }
//
//        if (overdueInvoices > 5) {
//            score += 20;
//        }
//
//        return Math.min(score, 100);
//    }
//
//    private RiskLevel mapRiskLevel(int score) {
//        if (score >= 70) return RiskLevel.HIGH;
//        if (score >= 40) return RiskLevel.MEDIUM;
//        return RiskLevel.LOW;
//    }
//
//    private String buildReasons(BigDecimal exposure, int maxDaysOverdue, int overdueInvoices, Locale locale) {
//        List<RiskSignal> signals = new ArrayList<>();
//
//        if (maxDaysOverdue >= 1 && maxDaysOverdue <= 7) signals.add(RiskSignal.OVERDUE_1_7);
//        else if (maxDaysOverdue >= 8 && maxDaysOverdue <= 14) signals.add(RiskSignal.OVERDUE_8_14);
//        else if (maxDaysOverdue >= 15 && maxDaysOverdue <= 30) signals.add(RiskSignal.OVERDUE_15_30);
//        else if (maxDaysOverdue >= 31 && maxDaysOverdue <= 60) signals.add(RiskSignal.OVERDUE_31_60);
//        else if (maxDaysOverdue > 60) signals.add(RiskSignal.OVERDUE_60_PLUS);
//
//        if (overdueInvoices == 0 && exposure.compareTo(BigDecimal.ZERO) > 0) {
//            signals.add(RiskSignal.NEW_CLIENT);
//        }
//
//        return signals.stream()
//                .map(s -> s.getDescription(locale))  // pass locale here
//                .reduce((a, b) -> a + "; " + b)
//                .orElse(String.format("Exposure=%s, MaxDaysOverdue=%d, OverdueInvoices=%d",
//                        exposure, maxDaysOverdue, overdueInvoices));
//    }
//    private String buildRecommendation(RiskLevel level) {
//        return switch (level) {
//            case HIGH -> "Immediate review required. Consider credit limit reduction.";
//            case MEDIUM -> "Monitor payment behaviour closely.";
//            case LOW -> "Standard monitoring.";
//            case CRITICAL -> "immidiatly work";
//        };
//    }
//
//
//    private Map<String, Integer> resolveColumns(Row headerRow) {
//        Map<String, Integer> map = new HashMap<>();
//
//        for (Cell cell : headerRow) {
//            if (cell.getCellType() != CellType.STRING) continue;
//
//            String header = normalize(cell.getStringCellValue());
//            map.put(header, cell.getColumnIndex());
//        }
//
//        return map;
//    }
//
//    private String normalize(String value) {
//        if (value == null) return null;
//
//        return value
//                .toLowerCase()
//                .trim()
//                .replace("ą", "a")
//                .replace("ć", "c")
//                .replace("ę", "e")
//                .replace("ł", "l")
//                .replace("ń", "n")
//                .replace("ó", "o")
//                .replace("ś", "s")
//                .replace("ż", "z")
//                .replace("ź", "z");
//    }
//
//    private Integer findColumn(Map<String, Integer> map, String... names) {
//        for (String name : names) {
//            String normalized = normalize(name);
//            Integer index = map.get(normalized);
//            if (index != null) {
//                return index;
//            }
//        }
//        return null;
//    }
//
//
//    private BigDecimal getCellDecimal(Row row, Integer index) {
//        if (index == null) return null;
//
//        Cell cell = row.getCell(index);
//        if (cell == null) return null;
//
//        try {
//            return switch (cell.getCellType()) {
//                case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
//                case STRING -> new BigDecimal(cell.getStringCellValue());
//                default -> null;
//            };
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    private Integer getCellInteger(Row row, Integer index) {
//        if (index == null) return null;
//
//        Cell cell = row.getCell(index);
//        if (cell == null) return null;
//
//        try {
//            return switch (cell.getCellType()) {
//                case NUMERIC -> (int) cell.getNumericCellValue();
//                case STRING -> Integer.parseInt(cell.getStringCellValue());
//                default -> null;
//            };
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//}
//package com.example.demo.uploading.service;
//
//import com.example.demo.base.entity.*;
//import com.example.demo.base.repository.ClientRepository;
//import com.example.demo.base.repository.RiskAssessmentRepository;
//import com.example.demo.excelUpload.repository.CreditLimitRepository;
//import com.example.demo.excelUpload.repository.ReceivableAgingRepository;
//import com.example.demo.risk.RiskLevel;
//import com.example.demo.risk.RiskSignal;
//import com.example.demo.uploading.archive.ArchiveReason;
//import com.example.demo.uploading.archive.ArchiveService;
//import org.apache.poi.ss.usermodel.*;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.InputStream;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.*;
//
//@Service
//public class ClientExcelUploadService {
//
//    private final ClientRepository clientRepository;
//    private final CreditLimitRepository creditLimitRepository;
//    private final RiskAssessmentRepository riskRepository;
//    private final ReceivableAgingRepository agingRepository;
//    private final ArchiveService archiveService;
//
//    public ClientExcelUploadService(ClientRepository clientRepository,
//                                    CreditLimitRepository creditLimitRepository,
//                                    RiskAssessmentRepository riskRepository,
//                                    ReceivableAgingRepository agingRepository,
//                                    ArchiveService archiveService) {
//        this.clientRepository = clientRepository;
//        this.creditLimitRepository = creditLimitRepository;
//        this.riskRepository = riskRepository;
//        this.agingRepository = agingRepository;
//        this.archiveService = archiveService;
//    }
//
//    @Transactional
//    public void upload(InputStream inputStream,
//                       String clientId,
//                       String clientName,
//                       boolean replaceConfirmed) throws Exception, ClientAlreadyExistsException {
//
//        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
//            Sheet sheet = workbook.getSheetAt(0);
//            if (sheet.getPhysicalNumberOfRows() < 2) {
//                throw new RuntimeException("Excel file is empty");
//            }
//
//            Map<String, Integer> columns = resolveColumns(sheet.getRow(0));
//            Integer saldoCol = findColumn(columns, "saldo");
//            Integer daysCol = findColumn(columns, "dni po terminie");
//
//            if (saldoCol == null) throw new RuntimeException("Missing required column: saldo");
//
//            Optional<Client> existingOpt = clientRepository.findByExternalId(clientId);
//
//            if (existingOpt.isPresent() && !replaceConfirmed) {
//                throw new ClientAlreadyExistsException(clientId);
//            }
//
//            if (existingOpt.isPresent() && replaceConfirmed) {
//                archiveService.archiveClientWithInvoices(
//                        existingOpt.get(),
//                        ArchiveReason.REPLACED_BY_UPLOAD
//                );
//            }
//
//            Client client = existingOpt.orElse(
//                    Client.builder(clientId, clientName, ClientStatus.ACTIVE)
//                            .creditLimit(BigDecimal.ZERO)
//                            .build()
//            );
//
//            client.setFullName(clientName);
//            client.setStatus(ClientStatus.ACTIVE);
//            clientRepository.save(client);
//
//            // ===== Aging calculation =====
//            BigDecimal totalExposure = BigDecimal.ZERO;
//            BigDecimal notDue = BigDecimal.ZERO;
//            BigDecimal overdue1to7 = BigDecimal.ZERO;
//            BigDecimal overdue8to14 = BigDecimal.ZERO;
//            BigDecimal overdue15to30 = BigDecimal.ZERO;
//            BigDecimal overdue31to60 = BigDecimal.ZERO;
//            BigDecimal overdue61to90 = BigDecimal.ZERO;
//            BigDecimal overdue91to120 = BigDecimal.ZERO;
//            BigDecimal overdue121to360 = BigDecimal.ZERO;
//            BigDecimal overdueAbove360 = BigDecimal.ZERO;
//
//            int maxDaysOverdue = 0;
//            int overdueInvoices = 0;
//
//            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
//                Row row = sheet.getRow(i);
//                if (row == null) continue;
//
//                BigDecimal saldo = getCellDecimal(row, saldoCol);
//                if (saldo == null || saldo.compareTo(BigDecimal.ZERO) <= 0) continue;
//
//                totalExposure = totalExposure.add(saldo);
//
//                Integer days = getCellInteger(row, daysCol);
//                if (days == null || days <= 0) {
//                    notDue = notDue.add(saldo);
//                    continue;
//                }
//
//                overdueInvoices++;
//                maxDaysOverdue = Math.max(maxDaysOverdue, days);
//
//                if (days <= 7) overdue1to7 = overdue1to7.add(saldo);
//                else if (days <= 14) overdue8to14 = overdue8to14.add(saldo);
//                else if (days <= 30) overdue15to30 = overdue15to30.add(saldo);
//                else if (days <= 60) overdue31to60 = overdue31to60.add(saldo);
//                else if (days <= 90) overdue61to90 = overdue61to90.add(saldo);
//                else if (days <= 120) overdue91to120 = overdue91to120.add(saldo);
//                else if (days <= 360) overdue121to360 = overdue121to360.add(saldo);
//                else overdueAbove360 = overdueAbove360.add(saldo);
//            }
//
//            // Remove previous aging
//            agingRepository.deleteByClient(client);
//
//            ReceivableAging aging = new ReceivableAging();
//            aging.setClient(client);
//            aging.setReportDate(LocalDate.now());
//            aging.setCalculatedAt(LocalDateTime.now());
//            aging.setTotalAmount(totalExposure);
//            aging.setNotDue(notDue);
//            aging.setOverdue1to7(overdue1to7);
//            aging.setOverdue8to14(overdue8to14);
//            aging.setOverdue15to30(overdue15to30);
//            aging.setOverdue31to60(overdue31to60);
//            aging.setOverdue61to90(overdue61to90);
//            aging.setOverdue91to120(overdue91to120);
//            aging.setOverdue121to360(overdue121to360);
//            aging.setOverdueAbove360(overdueAbove360);
//
//            agingRepository.save(aging);
//
//            // ===== Credit limit setup =====
//            CreditLimit limit = creditLimitRepository.findByClient(client)
//                    .orElseGet(() -> new CreditLimit.Builder(client,
//                            BigDecimal.valueOf(10000), // default limit
//                            30) // default payment terms
//                            .usedAmount(BigDecimal.ZERO)
//                            .validFrom(LocalDate.now())
//                            .validTo(LocalDate.now().plusYears(10))
//                            .build());
//
//            creditLimitRepository.save(limit);
//
//            // ===== Risk calculation =====
//            int riskScore = calculateRiskScore(totalExposure, maxDaysOverdue, overdueInvoices);
//            RiskLevel riskLevel = mapRiskLevel(riskScore);
//
//            RiskAssessment risk = new RiskAssessment.Builder(
//                    client,
//                    riskScore,
//                    riskLevel,
//                    LocalDateTime.now()
//            )
//                    .reasons(buildReasons(totalExposure, maxDaysOverdue, overdueInvoices))
//                    .recommendation(buildRecommendation(riskLevel))
//                    .build();
//
//            riskRepository.save(risk);
//        }
//    }
//
//    // === Risk logic ===
//    private int calculateRiskScore(BigDecimal exposure, int maxDaysOverdue, int overdueInvoices) {
//        int score = 0;
//        if (exposure.compareTo(new BigDecimal("100000")) > 0) score += 40;
//        if (maxDaysOverdue > 90) score += 40;
//        else if (maxDaysOverdue > 30) score += 20;
//        if (overdueInvoices > 5) score += 20;
//        return Math.min(score, 100);
//    }
//
//    private RiskLevel mapRiskLevel(int score) {
//        if (score >= 70) return RiskLevel.HIGH;
//        if (score >= 40) return RiskLevel.MEDIUM;
//        return RiskLevel.LOW;
//    }
//
//    private String buildReasons(BigDecimal exposure, int maxDaysOverdue, int overdueInvoices) {
//        List<RiskSignal> signals = new ArrayList<>();
//        Locale locale = Locale.getDefault();
//
//        if (maxDaysOverdue >= 1 && maxDaysOverdue <= 7) signals.add(RiskSignal.OVERDUE_1_7);
//        else if (maxDaysOverdue <= 14) signals.add(RiskSignal.OVERDUE_8_14);
//        else if (maxDaysOverdue <= 30) signals.add(RiskSignal.OVERDUE_15_30);
//        else if (maxDaysOverdue <= 60) signals.add(RiskSignal.OVERDUE_31_60);
//        else if (maxDaysOverdue > 60) signals.add(RiskSignal.OVERDUE_60_PLUS);
//
//        if (overdueInvoices == 0 && exposure.compareTo(BigDecimal.ZERO) > 0)
//            signals.add(RiskSignal.NEW_CLIENT);
//
//        if (signals.isEmpty()) {
//            return String.format("Exposure=%s, MaxDaysOverdue=%d, OverdueInvoices=%d",
//                    exposure, maxDaysOverdue, overdueInvoices);
//        }
//
//        return signals.stream().map(s -> s.getDescription(locale)).reduce((a, b) -> a + "; " + b).orElse("");
//    }
//
//    private String buildRecommendation(RiskLevel level) {
//        return switch (level) {
//            case HIGH -> "Immediate review required. Consider credit limit reduction.";
//            case MEDIUM -> "Monitor payment behaviour closely.";
//            case LOW -> "Standard monitoring.";
//            case CRITICAL -> "Immediate intervention required.";
//        };
//    }
//
//    // === Excel utilities ===
//    private Map<String, Integer> resolveColumns(Row headerRow) {
//        Map<String, Integer> map = new HashMap<>();
//        for (Cell cell : headerRow) {
//            if (cell.getCellType() != CellType.STRING) continue;
//            map.put(normalize(cell.getStringCellValue()), cell.getColumnIndex());
//        }
//        return map;
//    }
//
//    private String normalize(String value) {
//        if (value == null) return null;
//        return value.toLowerCase().trim()
//                .replace("ą", "a").replace("ć", "c").replace("ę", "e")
//                .replace("ł", "l").replace("ń", "n").replace("ó", "o")
//                .replace("ś", "s").replace("ż", "z").replace("ź", "z");
//    }
//
//    private Integer findColumn(Map<String, Integer> map, String... names) {
//        for (String name : names) {
//            Integer index = map.get(normalize(name));
//            if (index != null) return index;
//        }
//        return null;
//    }
//
//    private BigDecimal getCellDecimal(Row row, Integer index) {
//        if (index == null) return null;
//        Cell cell = row.getCell(index);
//        if (cell == null) return null;
//
//        try {
//            return switch (cell.getCellType()) {
//                case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
//                case STRING -> {
//                    String raw = cell.getStringCellValue();
//                    if (raw == null || raw.isBlank()) yield null;
//                    raw = raw.trim().replace(" ", "").replace(",", ".");
//                    yield new BigDecimal(raw);
//                }
//                default -> null;
//            };
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    private Integer getCellInteger(Row row, Integer index) {
//        if (index == null) return null;
//        Cell cell = row.getCell(index);
//        if (cell == null) return null;
//
//        try {
//            return switch (cell.getCellType()) {
//                case NUMERIC -> (int) cell.getNumericCellValue();
//                case STRING -> {
//                    String raw = cell.getStringCellValue();
//                    if (raw == null || raw.isBlank()) yield null;
//                    yield Integer.parseInt(raw.trim());
//                }
//                default -> null;
//            };
//        } catch (Exception e) {
//            return null;
//        }
//    }
//}