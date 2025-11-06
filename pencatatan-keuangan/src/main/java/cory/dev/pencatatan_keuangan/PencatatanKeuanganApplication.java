package cory.dev.pencatatan_keuangan;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import cory.dev.pencatatan_keuangan.model.FinancialRecord;
import cory.dev.pencatatan_keuangan.service.CsvInputService;
import cory.dev.pencatatan_keuangan.service.FinancialService;
import cory.dev.pencatatan_keuangan.service.GitHubService;

@SpringBootApplication
public class PencatatanKeuanganApplication {

	public static void main(String[] args) {
		SpringApplication.run(PencatatanKeuanganApplication.class, args);
	}

	@Bean
    public CommandLineRunner run(FinancialService financialService, GitHubService gitHubService, CsvInputService csvInputService) {
        return args -> {
            System.out.println("=== Finance Tracker - Input User ===\n");
            
            // Tampilkan data yang sudah ada dari CSV
            var existingRecords = financialService.getAllRecords();
            if (!existingRecords.isEmpty()) {
                System.out.println("📋 Data Existing dari CSV:");
                System.out.println("Tanggal\t\tSaldo Akhir\tPerubahan");
                System.out.println("-".repeat(60));
                
                for (var record : existingRecords) {
                    System.out.printf("%s\t%s\t%s%n",
                        record.getDate(),
                        record.getFormattedBalance(),
                        record.getFormattedChange()
                    );
                }
                System.out.println();
            }
            
            // Input dari user
            Scanner scanner = new Scanner(System.in);

            // --- TAHAP SINKRONISASI ---
            try {
                String remoteContent = gitHubService.downloadFileContent();
                String localContent = null;
                if (Files.exists(Paths.get("financial_records.csv"))) {
                    localContent = Files.readString(Paths.get("financial_records.csv"));
                }

                // Normalisasi line endings untuk perbandingan yang akurat
                String normalizedRemote = remoteContent != null ? remoteContent.replaceAll("\\r\\n", "\n") : null;
                String normalizedLocal = localContent != null ? localContent.replaceAll("\\r\\n", "\n") : null;

                if (normalizedRemote != null && !normalizedRemote.equals(normalizedLocal)) {
                    System.out.println("\n⚠️  PERINGATAN: Versi file di GitHub berbeda dengan versi lokal!");
                    System.out.println("Silakan pilih versi data yang ingin Anda gunakan:");
                    System.out.println("  1. Gunakan Versi Lokal (Perubahan lokal Anda akan di-upload saat keluar)");
                    System.out.println("  2. Gunakan Versi GitHub (Perubahan lokal Anda akan hilang)");

                    String choice = "";
                    while (!choice.equals("1") && !choice.equals("2")) {
                        System.out.print("Pilih (1 atau 2): ");
                        choice = scanner.nextLine().trim();
                    }

                    if (choice.equals("2")) {
                        System.out.println("    -> Memuat data dari GitHub...");
                        List<FinancialRecord> remoteRecords = csvInputService.parseCsvContent(remoteContent);
                        financialService.loadRecordsFromSource(remoteRecords);
                    } else {
                        System.out.println("    -> Tetap menggunakan data lokal.");
                    }
                } else if (normalizedRemote == null && normalizedLocal != null) {
                     System.out.println("    -> Info: Data hanya ada di lokal. Akan di-upload saat keluar.");
                } else {
                    System.out.println("    -> Info: Versi lokal dan GitHub sudah sinkron.");
                }

            } catch (Exception e) {
                System.err.println("❌ Gagal melakukan sinkronisasi dengan GitHub: " + e.getMessage());
            }
            
            // --- AKHIR TAHAP SINKRONISASI ---

            // Menampilkan data yang sudah ada (setelah sinkronisasi)
            // ... (kode untuk menampilkan data yang sudah ada seperti sebelumnya)

            // Lanjut ke menu utama
            
            while (true) {
                System.out.println("\n--- Menu ---");
                System.out.println("1. Tambah Saldo Baru");
                System.out.println("2. Lihat Semua Data");
                System.out.println("3. Keluar");
                System.out.print("Pilih menu (1-3): ");
                
                String choice = scanner.nextLine().trim();
                
                switch (choice) {
                    case "1":
                        addNewBalance(scanner, financialService);
                        break;
                    case "2":
                        displayAllData(financialService);
                        break;
                    case "3":
                        System.out.println("✓ Terima kasih, aplikasi ditutup.");
                        scanner.close();
                        System.exit(0);
                        break;
                    default:
                        System.out.println("❌ Pilihan tidak valid");
                }
            }
        };
    }
    
    private void addNewBalance(Scanner scanner, FinancialService financialService) {
        try {
            System.out.print("Masukkan tanggal (YYYY-MM-DD) [default: hari ini]: ");
            String dateStr = scanner.nextLine().trim();
            LocalDate date;
            
            if (dateStr.isEmpty()) {
                date = LocalDate.now();
            } else {
                date = LocalDate.parse(dateStr);
            }
            
            System.out.print("Masukkan saldo (contoh: 4489799 atau 4.489.799): ");
            String balanceStr = scanner.nextLine().trim();
            
            // Parse saldo - bisa dengan atau tanpa pemisah
            String cleanedBalance = balanceStr.replaceAll("\\.", "");
            BigDecimal balance = new BigDecimal(cleanedBalance);
            
            // Add record
            financialService.addRecord(date, balance);
            
            System.out.println("\n✓ Saldo berhasil ditambahkan!");
            System.out.println("  Tanggal: " + date);
            System.out.println("  Saldo: Rp " + formatCurrency(balance));
            
            // Cek apakah ada perubahan
            var records = financialService.getAllRecords();
            if (records.size() > 1) {
                var lastRecord = records.get(records.size() - 1);
                if (lastRecord.getChange() != null) {
                    System.out.println("  Perubahan: " + lastRecord.getFormattedChange());
                }
            }
            
        } catch (NumberFormatException e) {
            System.out.println("❌ Format saldo tidak valid!");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    
    private void displayAllData(FinancialService financialService) {
        var records = financialService.getAllRecords();
        
        if (records.isEmpty()) {
            System.out.println("⚠️  Belum ada data saldo.");
            return;
        }
        
        System.out.println("\n📊 Semua Data Saldo Keuangan:");
        System.out.println("Tanggal\t\tSaldo Akhir\tPerubahan");
        System.out.println("-".repeat(60));
        
        for (var record : records) {
            System.out.printf("%s\t%s\t%s%n",
                record.getDate(),
                record.getFormattedBalance(),
                record.getFormattedChange()
            );
        }
        
        System.out.println("\n✓ File CSV: financial_records.csv");
    }
    
    private static String formatCurrency(BigDecimal amount) {
        String numberStr = amount.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
        StringBuilder formatted = new StringBuilder();
        
        int count = 0;
        for (int i = numberStr.length() - 1; i >= 0; i--) {
            if (count == 3) {
                formatted.insert(0, '.');
                count = 0;
            }
            formatted.insert(0, numberStr.charAt(i));
            count++;
        }
        
        return formatted.toString();
    }

}
