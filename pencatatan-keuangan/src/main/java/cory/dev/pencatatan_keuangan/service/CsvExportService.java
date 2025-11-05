package cory.dev.pencatatan_keuangan.service;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import cory.dev.pencatatan_keuangan.model.FinancialRecord;

@Service
public class CsvExportService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String CSV_FILENAME = "financial_records.csv";
    
    public String exportToCsv(List<FinancialRecord> records) throws IOException {
        
        try (FileWriter fileWriter = new FileWriter(CSV_FILENAME);
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT
                     .withHeader("Tanggal", "Saldo Akhir", "Perubahan"))) {
            
            for (FinancialRecord record : records) {
                csvPrinter.printRecord(
                    record.getDate().format(DATE_FORMATTER),
                    record.getFormattedBalance(),
                    record.getFormattedChange()
                );
            }
            
            csvPrinter.flush();
        }
        
        return CSV_FILENAME;
    }
}
