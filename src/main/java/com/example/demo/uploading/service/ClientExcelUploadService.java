package com.example.demo.uploading.service;

import com.example.demo.base.dto.UploadResult;
import com.example.demo.base.entity.*;
import com.example.demo.base.repository.ClientRepository;
import com.example.demo.base.repository.RiskAssessmentRepository;
import com.example.demo.base.service.CreditLimitService;
import com.example.demo.excelUpload.repository.CreditLimitRepository;
import com.example.demo.excelUpload.repository.ReceivableAgingRepository;
import com.example.demo.risk.RiskLevel;
import com.example.demo.risk.RiskSignal;
import com.example.demo.uploading.archive.ArchiveReason;
import com.example.demo.uploading.archive.ArchiveService;
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
    public UploadResult uploadAndReturnResult(
            InputStream inputStream,
            String clientId,
            String clientName,
            boolean replaceConfirmed,
            Locale locale
    ) throws Exception, ClientAlreadyExistsException {

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() < 2) {
                throw new RuntimeException("Excel file is empty");
            }

            Map<String, Integer> columns = resolveColumns(sheet.getRow(0));
            Integer amountCol = findColumn(columns, "amount", "value", "total", "exposure", "kwota", "saldo");
            Integer daysCol = findColumn(columns, "days overdue", "days", "overdue_days", "dni po terminie");

            if (amountCol == null) {
                throw new RuntimeException("Missing required column: amount");
            }

            // --- 1. Отримуємо існуючого клієнта ---
            Optional<Client> existingOpt = clientRepository.findByExternalId(clientId);

            if (existingOpt.isPresent() && !replaceConfirmed) {
                throw new ClientAlreadyExistsException(clientId);
            }

            // --- 2. Архівуємо старого клієнта перед створенням нового ---
            existingOpt.ifPresent(client ->
                    archiveService.archiveClientWithInvoices(client, ArchiveReason.REPLACED_BY_UPLOAD)
            );

            // --- 3. Створюємо нового клієнта або оновлюємо існуючого ---
            Client client = existingOpt.orElse(
                    Client.builder(clientId, clientName, ClientStatus.ACTIVE)
                            .creditLimit(BigDecimal.ZERO)
                            .build()
            );
            client.setFullName(clientName);
            client.setStatus(ClientStatus.ACTIVE);
            client = clientRepository.save(client);

            // --- 4. Обробка Excel: сумарна експозиція і прострочені рахунки ---
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

            // --- 5. Розрахунок ризику ---
            int riskScore = calculateRiskScore(totalExposure, maxDaysOverdue, overdueInvoices);
            RiskLevel riskLevelEnum = mapRiskLevel(riskScore);
            String reasons = buildReasons(totalExposure, maxDaysOverdue, overdueInvoices, locale);
            String recommendation = buildRecommendation(riskLevelEnum);
            LocalDateTime calculatedAt = LocalDateTime.now();

            RiskAssessment risk = new RiskAssessment.Builder(client, riskScore, riskLevelEnum, calculatedAt)
                    .reasons(reasons)
                    .recommendation(recommendation)
                    .build();
            riskRepository.save(risk);

            // --- 6. CreditLimit: беремо останній або створюємо новий ---
            List<CreditLimit> limits = creditLimitRepository.findAllByClient(client);
            CreditLimit creditLimit;
            if (limits.isEmpty()) {
                creditLimit = new CreditLimit();
                creditLimit.setClient(client);
            } else {
                // беремо останній по validFrom
                Client finalClient = client;
                creditLimit = limits.stream()
                        .max(Comparator.comparing(CreditLimit::getValidFrom))
                        .orElseGet(() -> {
                            CreditLimit cl = new CreditLimit();
                            cl.setClient(finalClient);
                            return cl;
                        });
            }
            creditLimit.setLimitAmount(client.getCreditLimit() != null ? client.getCreditLimit() : BigDecimal.ZERO);
            creditLimit.setUsedAmount(totalExposure);
            creditLimit.setPaymentTermsDays(maxDaysOverdue);
            creditLimit.setValidFrom(LocalDate.now());
            creditLimitRepository.save(creditLimit);

            // --- 7. ReceivableAging: беремо останній або створюємо новий ---
            List<ReceivableAging> agings = agingRepository.findAllByClient(client);
            ReceivableAging aging;
            if (agings.isEmpty()) {
                aging = new ReceivableAging();
                aging.setClient(client);
            } else {
                Client finalClient1 = client;
                aging = agings.stream()
                        .max(Comparator.comparing(ReceivableAging::getReportDate))
                        .orElseGet(() -> {
                            ReceivableAging ra = new ReceivableAging();
                            ra.setClient(finalClient1);
                            return ra;
                        });
            }
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
            aging.setLegalEvents(BigDecimal.ZERO.toString());
            aging.setCalculatedAt(LocalDateTime.now());
            agingRepository.save(aging);

            // --- 8. Повертаємо результат завантаження ---
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
//    @Transactional
//    public UploadResult uploadAndReturnResult(InputStream inputStream,
//                                              String clientId,
//                                              String clientName,
//                                              boolean replaceConfirmed,
//                                              Locale locale)
//            throws Exception, ClientAlreadyExistsException {
//
//        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
//
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
//
//            if (existingOpt.isPresent() && !replaceConfirmed) {
//                throw new ClientAlreadyExistsException(clientId);
//            }
//
//            existingOpt.ifPresent(client ->
//                    archiveService.archiveClientWithInvoices(client, ArchiveReason.REPLACED_BY_UPLOAD)
//            );
//
//            Client client = existingOpt.orElse(
//                    Client.builder(clientId, clientName, ClientStatus.ACTIVE)
//                            .creditLimit(BigDecimal.ZERO)
//                            .build()
//            );
//
//            client.setFullName(clientName);
//            client.setStatus(ClientStatus.ACTIVE);
//            client = clientRepository.save(client);
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
//
//                if (daysOverdue != null && daysOverdue > 0) {
//                    overdueInvoices++;
//                    maxDaysOverdue = Math.max(maxDaysOverdue, daysOverdue);
//                }
//            }
//
//            int riskScore = calculateRiskScore(totalExposure, maxDaysOverdue, overdueInvoices);
//            RiskLevel riskLevelEnum = mapRiskLevel(riskScore);
//
//            String reasons = buildReasons(totalExposure, maxDaysOverdue, overdueInvoices, locale);
//            String recommendation = buildRecommendation(riskLevelEnum);
//            LocalDateTime calculatedAt = LocalDateTime.now();
//
//            RiskAssessment risk = new RiskAssessment.Builder(client, riskScore, riskLevelEnum, calculatedAt)
//                    .reasons(reasons)
//                    .recommendation(recommendation)
//                    .build();
//
//            riskRepository.save(risk);
//
//
//            CreditLimit creditLimit = new CreditLimit();
//            creditLimit.setClient(client);
//            creditLimit.setLimitAmount(client.getCreditLimit() != null ? client.getCreditLimit() : BigDecimal.ZERO);
//            creditLimit.setUsedAmount(totalExposure);
//            creditLimit.setPaymentTermsDays(maxDaysOverdue);
//            creditLimit.setValidFrom(LocalDate.now());
//            creditLimitRepository.save(creditLimit);
//
//            ReceivableAging aging = new ReceivableAging();
//            aging.setClient(client);
//            aging.setReportDate(LocalDate.now());
//            aging.setTotalAmount(totalExposure);
//            aging.setPaymentTermsDays(maxDaysOverdue);
//
//            aging.setNotDue(BigDecimal.ZERO);
//            aging.setOverdue1to7(BigDecimal.ZERO);
//            aging.setOverdue8to14(BigDecimal.ZERO);
//            aging.setOverdue15to30(BigDecimal.ZERO);
//            aging.setOverdue31to60(BigDecimal.ZERO);
//            aging.setOverdue61to90(BigDecimal.ZERO);
//            aging.setOverdue91to120(BigDecimal.ZERO);
//            aging.setOverdue121to360(BigDecimal.ZERO);
//            aging.setOverdueAbove360(BigDecimal.ZERO);
//            aging.setLegalEvents(String.valueOf(BigDecimal.ZERO));
//            aging.setCalculatedAt(LocalDateTime.now());
//            agingRepository.save(aging);
//
//            // === BUILD RESULT DTO ===
//            UploadResult result = new UploadResult();
//            result.setClientId(client.getExternalId());
//            result.setClientName(client.getName());
//            result.setCreditLimit(creditLimit.getLimitAmount());
//            result.setExposure(totalExposure);
//            result.setAverageDaysPastDue(overdueInvoices > 0 ? maxDaysOverdue : null);
//            result.setRiskScore(riskScore);
//            result.setRiskLevel(riskLevelEnum.name());
//            result.setReasons(reasons);
//            result.setRecommendation(recommendation);
//            result.setCalculationDate(calculatedAt);
//
//            return result;
//        }
//    }


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