package cory.dev.pencatatan_keuangan.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import cory.dev.pencatatan_keuangan.model.FinancialRecord;
import lombok.RequiredArgsConstructor;

@Service
public class FinancialService {
    private final List<FinancialRecord> records = new ArrayList<>();
    private final CsvInputService csvInputService;
    private final CsvExportService csvExportService;

    public FinancialService(CsvInputService csvInputService, CsvExportService csvExportService) {
        this.csvInputService = csvInputService;
        this.csvExportService = csvExportService;
        
        // Load existing records dari CSV saat aplikasi start
        try {
            this.records.addAll(csvInputService.readFromCsv());
        } catch (IOException e) {
            System.err.println("Warning: Tidak bisa membaca CSV file: " + e.getMessage());
        }
    }

    public void addRecord(LocalDate date, BigDecimal balance) {
        // Cek apakah record untuk tanggal ini sudah ada
        records.removeIf(r -> r.getDate().equals(date));
        
        // Hitung perubahan dari record terakhir di CSV/memory
        BigDecimal lastBalance = getLastBalance();
        BigDecimal change = null;
        
        if (lastBalance != null) {
            change = balance.subtract(lastBalance);
        }
        
        FinancialRecord record = new FinancialRecord(date, balance, change);
        records.add(record);
        
        // Sort berdasarkan tanggal
        records.sort(Comparator.comparing(FinancialRecord::getDate));
        
        // Auto-save ke CSV setelah setiap penambahan
        try {
            saveToCSV();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private BigDecimal getLastBalance() {
        if (!records.isEmpty()) {
            // Cari saldo terakhir dari records yang sudah dimuat
            return records.stream()
                .max(Comparator.comparing(FinancialRecord::getDate))
                .map(FinancialRecord::getBalance)
                .orElse(null);
        }
        return null;
    }
    
    public void saveToCSV() throws IOException {
        csvExportService.exportToCsv(records);
    }
    
    private BigDecimal calculateChange(LocalDate date, BigDecimal currentBalance) {
        return records.stream()
            .filter(r -> r.getDate().isBefore(date))
            .max(Comparator.comparing(FinancialRecord::getDate))
            .map(r -> currentBalance.subtract(r.getBalance()))
            .orElse(null);
    }

    public void loadRecordsFromSource(List<FinancialRecord> sourceRecords) {
        this.records.clear();
        this.records.addAll(sourceRecords);
        // Pastikan data tetap terurut setelah dimuat
        this.records.sort(Comparator.comparing(FinancialRecord::getDate));
        System.out.println("    -> Data di memori telah disinkronkan dengan sumber yang dipilih.");
    }
    
    public List<FinancialRecord> getAllRecords() {
        return new ArrayList<>(records);
    }
    
    public void clearRecords() {
        records.clear();
    }
    
    public boolean isEmpty() {
        return records.isEmpty();
    }
}
