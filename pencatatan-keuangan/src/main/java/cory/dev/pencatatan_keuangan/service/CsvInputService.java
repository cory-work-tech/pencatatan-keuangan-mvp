package cory.dev.pencatatan_keuangan.service;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import cory.dev.pencatatan_keuangan.model.FinancialRecord;

@Service
public class CsvInputService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String CSV_FILENAME = "financial_records.csv";
    
    /**
     * Baca file CSV yang sudah ada dan kembalikan semua records
     */
    public List<FinancialRecord> readFromCsv() throws IOException {
        List<FinancialRecord> records = new ArrayList<>();
        
        // Cek apakah file CSV ada
        if (!Files.exists(Paths.get(CSV_FILENAME))) {
            return records; // Return empty list jika file tidak ada
        }
        
        try (FileReader fileReader = new FileReader(CSV_FILENAME);
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {
            
            for (CSVRecord csvRecord : csvParser) {
                try {
                    String tanggal = csvRecord.get("Tanggal");
                    String saldoStr = csvRecord.get("Saldo Akhir");
                    String perubahanStr = csvRecord.get("Perubahan");
                    
                    LocalDate date = LocalDate.parse(tanggal, DATE_FORMATTER);
                    BigDecimal balance = parseCurrency(saldoStr);
                    BigDecimal change = parseCurrency(perubahanStr);
                    
                    records.add(new FinancialRecord(date, balance, change));
                } catch (Exception e) {
                    System.err.println("Error parsing CSV record: " + e.getMessage());
                }
            }
        }
        
        return records;
    }
    
    /**
     * Parse currency format "Rp X.XXX.XXX" menjadi BigDecimal
     */
    private BigDecimal parseCurrency(String currencyStr) {
        if (currencyStr == null || currencyStr.trim().isEmpty() || currencyStr.equals("Rp 0")) {
            return null;
        }
        
        // Remove "Rp ", "+", dan "-"
        String cleaned = currencyStr.replaceAll("[Rp\\s+]", "").trim();
        if (cleaned.startsWith("-")) {
            cleaned = "-" + cleaned.substring(1).replaceAll("\\.", "");
        } else {
            cleaned = cleaned.replaceAll("\\.", "");
        }
        
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Dapatkan saldo terakhir dari file CSV
     */
    public BigDecimal getLastBalance() throws IOException {
        List<FinancialRecord> records = readFromCsv();
        if (records.isEmpty()) {
            return null;
        }
        
        // Sort by date dan ambil yang terakhir
        return records.stream()
            .max(Comparator.comparing(FinancialRecord::getDate))
            .map(FinancialRecord::getBalance)
            .orElse(null);
    }
    
    /**
     * Dapatkan tanggal terakhir dari file CSV
     */
    public LocalDate getLastDate() throws IOException {
        List<FinancialRecord> records = readFromCsv();
        if (records.isEmpty()) {
            return null;
        }
        
        return records.stream()
            .map(FinancialRecord::getDate)
            .max(Comparator.naturalOrder())
            .orElse(null);
    }
}
