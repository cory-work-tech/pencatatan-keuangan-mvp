package cory.dev.pencatatan_keuangan.config;

import org.springframework.stereotype.Component;

import cory.dev.pencatatan_keuangan.service.FinancialService;
import cory.dev.pencatatan_keuangan.service.GitHubService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ShutdownManager {
    private final FinancialService financialService;
    private final GitHubService gitHubService;

    @PreDestroy
    public void onShutdown() {
        System.out.println("\n🚦 Aplikasi akan berhenti, menjalankan proses shutdown...");

        try {
            // Langkah 1: Simpan semua data dari memori ke file CSV lokal
            System.out.println("    -> Menyimpan data terakhir ke CSV lokal...");
            financialService.saveToCSV();
            System.out.println("    -> CSV lokal berhasil disimpan.");

            // Langkah 2: Upload file CSV yang sudah di-update ke GitHub
            gitHubService.uploadFile();

        } catch (Exception e) {
            System.err.println("❌ Terjadi error saat proses shutdown: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("👋 Shutdown process complete. Have a great day!");
    }

}
