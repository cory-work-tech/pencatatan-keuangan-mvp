package cory.dev.pencatatan_keuangan.service;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;

public class InputFIleParser {
    private static final String INPUT_FILENAME = "input.txt";
    public static class InputEntry {
        public LocalDate date;
        public BigDecimal balance;
        
        public InputEntry(LocalDate date, BigDecimal balance) {
            this.date = date;
            this.balance = balance;
        }
    }

    public static java.util.List<InputEntry> readInputFile() throws Exception {
        java.util.List<InputEntry> entries = new java.util.ArrayList<>();
        
        if (!Files.exists(Paths.get(INPUT_FILENAME))) {
            System.out.println("⚠️  File input.txt tidak ditemukan.");
            return entries;
        }
        
        java.util.List<String> lines = Files.readAllLines(Paths.get(INPUT_FILENAME));
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue; // Skip empty lines dan comments
            }
            
            try {
                String[] parts = line.split(":");
                if (parts.length != 2) {
                    System.err.println("❌ Format tidak valid: " + line);
                    continue;
                }
                
                LocalDate date = LocalDate.parse(parts[0]);
                String balanceStr = parts[1].replaceAll("\\.", ""); // Remove pemisah ribuan
                BigDecimal balance = new BigDecimal(balanceStr);
                
                entries.add(new InputEntry(date, balance));
            } catch (Exception e) {
                System.err.println("❌ Error parsing line: " + line + " - " + e.getMessage());
            }
        }
        
        return entries;
    }
}
