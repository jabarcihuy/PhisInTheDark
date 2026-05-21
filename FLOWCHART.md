# Flowchart Arsitektur Phis in the Dark

Berikut adalah diagram alir (flowchart) yang menggambarkan bagaimana komponen-komponen di dalam game saling berinteraksi, mulai dari proses *boot*, integrasi database, hingga gameplay utama.

```mermaid
flowchart TD
    %% Styling
    classDef ui fill:#1E2B38,stroke:#31DCB0,stroke-width:2px,color:#E0E0E0
    classDef core fill:#232B32,stroke:#F5B700,stroke-width:2px,color:#E0E0E0
    classDef db fill:#0A3A40,stroke:#31DCB0,stroke-width:2px,color:#E0E0E0
    classDef event fill:#4A1525,stroke:#FF4D4D,stroke-width:2px,color:#E0E0E0
    classDef endNode fill:#111,stroke:#333,stroke-width:2px,color:#888

    %% Nodes
    Main([Game.main]) ---> Init[Inisialisasi Game]
    Init ---> LoginMenu[[LoginMenu UI]]:::ui
    
    subgraph DatabaseLayer [Database Layer]
        DBManager[(DatabaseManager)]:::db
        MariaDB[(MariaDB)]:::db
    end

    LoginMenu -->|Input Username/Password| DBManager
    DBManager -->|Query users| MariaDB
    MariaDB -->|Return data| DBManager
    
    DBManager -->|Login Berhasil| GameCore[Game.java]:::core
    
    GameCore -->|Inisialisasi| SaveManager[SaveManager]:::core
    GameCore -->|dbManager.seedDataIfEmpty| DBManager
    GameCore -->|dbManager.loadBrowser| BrowserObj[Objek Browser]:::core
    
    DBManager -->|Load websites & puzzles| MariaDB
    
    GameCore --> StartMenu[[StartMenu UI]]:::ui
    
    StartMenu -->|Pilih Mode & Load Save| GameCore
    
    GameCore -->|Load progress player| SaveManager
    SaveManager -->|Query save_slots| MariaDB
    
    GameCore --> DesktopUI[[DesktopUI]]:::ui
    GameCore --> EventSystem((EventSystem)):::event
    
    subgraph GameplayLoop [Gameplay Loop]
        DesktopUI --- Terminal[TerminalWindow]:::ui
        DesktopUI --- BrowserUI[BrowserWindow]:::ui
        DesktopUI --- Notes[TextWindow / Notes]:::ui
        
        BrowserUI -->|Akses Website| BrowserObj
        BrowserObj -->|Cek Jawaban| DatabasePuzzle[DatabasePuzzle]:::core
        DatabasePuzzle -->|Benar| GameCore
        
        GameCore -->|Simpan Progress| SaveManager
        SaveManager -->|INSERT/UPDATE saves| MariaDB
        
        EventSystem -.->|Trigger Random Event| Threat[Threats: Popup, Audio]:::event
        Threat -.-> DesktopUI
    end
    
    GameCore -->|Semua Puzzle Selesai| Ending[[Ending / Credits]]:::ui
    Ending --> Exit([Selesai]):::endNode
```

### Penjelasan Komponen:

1. **Titik Awal (Game.main)**: Game melakukan instansiasi terhadap `AudioManager` dan `DatabaseManager`, lalu membuka `LoginMenu`.
2. **Database Layer**: `DatabaseManager` memproses otentikasi login/register menggunakan hashing SHA-256 dan mencocokkan data di **MariaDB**.
3. **Pemuatan Data**: Setelah sukses login, game meminta `DatabaseManager` mengambil data puzzle dan struktur web, lalu merakitnya ke dalam virtual `Browser`.
4. **Start Menu**: Game menyuruh `SaveManager` membaca save data di database, lalu memberikan opsi ke player (Normal / Tutorial, New / Load).
5. **Gameplay (DesktopUI)**: Komponen utama di mana player menginvestigasi puzzle. Jika pemain menjawab *puzzle* dengan benar di browser, event dilempar ke `Game` untuk menyimpan progress melalui `SaveManager`.
6. **Event System**: Berjalan di *background thread* untuk menakut-nakuti pemain secara random ketika bermain di mode Normal (memunculkan jumpscare popup atau mengubah wallpaper).
