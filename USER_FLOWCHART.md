# Flowchart Alur Pemain (User Flow)

Ini adalah versi flowchart yang jauh lebih sederhana. Diagram ini hanya menampilkan sudut pandang perjalanan pemain (User Flow) saat memainkan Phis in the Dark, tanpa menampilkan kerumitan kode di belakang layar.

```mermaid
flowchart TD
    %% Styling
    classDef main fill:#1E2B38,stroke:#31DCB0,stroke-width:2px,color:#E0E0E0
    classDef gameplay fill:#232B32,stroke:#F5B700,stroke-width:2px,color:#E0E0E0
    classDef endNode fill:#111,stroke:#333,stroke-width:2px,color:#888

    %% Alur
    Start([Jalankan Game]) --> Login[Layar Login / Register]:::main
    Login -->|Berhasil Login| Menu[Main Menu]:::main
    Menu -->|Pilih Route & Load Data| Desktop[Masuk ke Desktop Virtual]:::gameplay
    
    subgraph Gameplay Loop
        Desktop -->|Investigasi| Browser[Buka Browser & Website]:::gameplay
        Browser -->|Pecahkan Kode| Puzzle{Cek Jawaban}:::gameplay
        Puzzle -->|Salah| Browser
        Puzzle -->|Benar| Save[Auto-Save Progress ke Database]:::main
        Save -->|Belum Cukup Syarat| Desktop
    end
    
    Save -->|Sudah Dapat 3 Kunci| Terminal[Buka Terminal & Ketik exit_trace]:::gameplay
    Terminal --> Ending[[Tamat]]:::main
    Ending --> Selesai([Selesai]):::endNode
```

### Penjelasan Singkat:
1. **Layar Login**: Pemain mendaftarkan diri atau masuk ke akunnya.
2. **Main Menu**: Pemain memilih mode (Tutorial / Normal) dan memuat *save data*.
3. **Desktop Virtual**: Pemain melakukan eksplorasi dengan membaca petunjuk.
4. **Gameplay Loop**: Pemain mencoba menebak jawaban puzzle. Jika benar, progress akan otomatis tersimpan. Proses ini diulang sampai mendapatkan 3 kunci.
5. **Tamat**: Setelah kunci lengkap, pemain meretas keluar via Terminal untuk menyelesaikan game.
