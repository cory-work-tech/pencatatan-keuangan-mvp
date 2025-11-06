package cory.dev.pencatatan_keuangan.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class GitHubService {
    private final String token;
    private final String repoOwner;
    private final String repoName;
    private static final String CSV_FILE_PATH = "financial_records.csv";
    private static final String COMMIT_MESSAGE = "Update financial records_"+LocalDateTime.now().toString();

    public GitHubService(
            @Value("${github.token}") String token,
            @Value("${github.repository.owner}") String repoOwner,
            @Value("${github.repository.name}") String repoName) {
        this.token = token;
        this.repoOwner = repoOwner;
        this.repoName = repoName;
    }


    public void uploadFile() throws IOException {
        System.out.println("🚀 Memulai proses upload ke GitHub...");

        // 1. Koneksi ke GitHub menggunakan token
        GitHub github = new GitHubBuilder().withOAuthToken(token).build();

        // 2. Dapatkan repositori tujuan
        GHRepository repo = github.getRepository(repoOwner + "/" + repoName);
        System.out.println("    -> Repositori ditemukan: " + repo.getFullName());

        // 3. Baca konten file CSV lokal
        String content = new String(Files.readAllBytes(Paths.get(CSV_FILE_PATH)));

        try {
            // 4. Cek apakah file sudah ada di repo untuk mendapatkan SHA (diperlukan untuk update)
            GHContent fileContent = repo.getFileContent(CSV_FILE_PATH);
            
            // Jika ada, update file
            fileContent.update(content, COMMIT_MESSAGE + " (update)");
            System.out.println("    -> File berhasil di-update di GitHub.");

        } catch (GHFileNotFoundException e) {
            // 5. Jika file tidak ditemukan, buat file baru
            repo.createContent()
                .path(CSV_FILE_PATH)
                .content(content)
                .message(COMMIT_MESSAGE + " (initial commit)")
                .commit();
            System.out.println("    -> File berhasil dibuat di GitHub.");
        }
        System.out.println("✅ Proses upload selesai.");
    }

    public String downloadFileContent() throws IOException {
    System.out.println("🔎 Mencoba mengambil file dari GitHub...");
    try {
        GitHub github = new GitHubBuilder().withOAuthToken(token).build();
        GHRepository repo = github.getRepository(repoOwner + "/" + repoName);
        GHContent fileContent = repo.getFileContent(CSV_FILE_PATH);
        
        System.out.println("    -> File ditemukan di GitHub. Mengunduh konten...");
        // Konten dari GitHub di-encode dalam Base64, jadi perlu di-decode
        return new String(fileContent.read().readAllBytes(), "UTF-8");

    } catch (GHFileNotFoundException e) {
        System.out.println("    -> Info: File tidak ditemukan di repositori GitHub.");
        return null; // Kembalikan null jika file tidak ada di repo
    }
}
}
