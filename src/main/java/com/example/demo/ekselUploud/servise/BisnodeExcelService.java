package com.example.demo.ekselUploud.servise;

import org.springframework.stereotype.Service;

@Service
public class BisnodeExcelService {

    private final BisnodeRepository repository;

    public BisnodeExcelService(BisnodeRepository repository) {
        this.repository = repository;
    }

    public void importBisnodeFile(File file) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // заголовок

                String clientName = row.getCell(0).getStringCellValue();
                Cell externalIdCell = row.getCell(1);
                Long externalId = externalIdCell.getCellType() == CellType.NUMERIC ?
                        (long) externalIdCell.getNumericCellValue() : null;
                Integer dax = (int) row.getCell(2).getNumericCellValue();
                String rating = row.getCell(3).getStringCellValue();

                repository.save(new Bisnode(
                        clientName,
                        externalId,
                        dax,
                        rating
                ));
            }
        }
    }
}
