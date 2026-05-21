# LAPORAN PRAKTIKUM

# PHIS IN THE DARK

## Game Desktop Java Swing Bertema Psychological Horror Hacking Simulator

**Nama:** ........................................  
**NIM:** .........................................  
**Kelas:** .......................................  
**Mata Kuliah:** Pemrograman Berorientasi Objek / Praktikum Pemrograman  
**Tanggal:** 15 Mei 2026  

---

## Abstrak

Phis in the Dark adalah game desktop berbasis Java Swing yang menggabungkan konsep psychological horror, simulasi hacking, dan pembelajaran logika pemrograman dasar. Game ini menggunakan tampilan fake desktop yang berisi browser, terminal, notes, mail, settings, sistem save/load online, popup ancaman, efek glitch, serta tiga puzzle utama. Pemain harus menyelesaikan tiga puzzle untuk memperoleh tiga key, yaitu `forum_key`, `server_route`, dan `trace_key` yang akan disimpan progresnya ke dalam Cloud Database (Railway/MariaDB). Setelah semua key terkumpul, pemain dapat menyelesaikan permainan melalui command `exit_trace` pada terminal. Berdasarkan pengujian smoke test, seluruh database schema, website, asset utama, puzzle, aturan progres, dan validasi jawaban berjalan dengan status OK.

## BAB I PENDAHULUAN

### 1.1 Latar Belakang

Perkembangan game edukasi memberi peluang untuk menyampaikan materi pemrograman dengan cara yang lebih interaktif. Materi dasar seperti variable, percabangan, dan perulangan sering dianggap abstrak oleh pemula. Oleh karena itu, dibuatlah prototype game desktop Phis in the Dark yang membungkus latihan logika pemrograman ke dalam pengalaman investigasi digital, didukung dengan penyimpanan cloud agar pemain dapat menyimpan progres dan akun mereka dengan aman.

### 1.2 Rumusan Masalah

1. Bagaimana merancang game desktop Java Swing dengan konsep fake operating system.
2. Bagaimana menerapkan konsep OOP pada struktur game dan integrasi koneksi Database.
3. Bagaimana membuat puzzle edukatif yang melatih variable, if else, dan loop.
4. Bagaimana mengintegrasikan sistem login, registrasi, inventory key, save/load, event horror, dan ending.
5. Bagaimana melakukan pengujian dasar untuk memastikan data tersimpan dengan benar ke database Railway.

### 1.3 Tujuan

1. Membuat playable prototype game desktop menggunakan Java Swing.
2. Menerapkan prinsip object-oriented programming pada class game, player, inventory, website, puzzle, database, UI, dan event.
3. Menyediakan sistem Save/Load online multi-user menggunakan MariaDB di Railway.
4. Melakukan smoke test untuk memvalidasi alur dan konektivitas database.

### 1.4 Batasan Masalah

1. Game dibuat sebagai desktop game menggunakan Java Swing.
2. Asset visual dan audio dasar digenerate secara lokal.
3. Koneksi ke Cloud Database Railway membutuhkan internet aktif.

## BAB II LANDASAN TEORI

### 2.1 Java Swing & Database (JDBC)
Swing digunakan untuk merancang antarmuka grafis. Selain itu, JDBC (Java Database Connectivity) digunakan untuk menghubungkan game dengan Cloud MariaDB Database yang dihost melalui platform Railway. File `.env` digunakan untuk menjaga keamanan kredensial.

### 2.2 Object-Oriented Programming
1. **Encapsulation:** Diterapkan pada `Player`, `Inventory`, dan `User`.
2. **Inheritance:** Diterapkan pada `BrowserWindow`, `TerminalWindow`, menggunakan dasar `BaseWindow`.
3. **Polymorphism:** Sistem threat melalui interface `Threat`.
4. **Abstraction:** Class abstrak `DatabaseManager` dan `Puzzle`.

### 2.3 Game Loop dan Progression
Progression dikendalikan oleh tiga puzzle utama dan disinkronisasikan ke tabel `save_solved_puzzles` dan `save_inventory_items` di database. 

## BAB III ANALISIS DAN PERANCANGAN

### 3.1 Gambaran Umum Sistem
Saat game dijalankan, pemain harus Login atau Register. Setelah masuk, pemain melihat start menu dengan pilihan Game Baru, Load Game, dan Tutorial. Game menggunakan fake desktop berisi:
1. Browser untuk membuka website.
2. Terminal untuk menjalankan command.
3. Notes, Mail, Settings, dan fitur Save/Load Online.

<!-- MASUKKAN SCREENSHOT DI SINI: Screenshot tampilan layar Login/Register saat game pertama kali dijalankan. -->

### 3.2 Struktur Folder

```text
src/main/java/com/phisinthedark/
  audio/      Audio manager
  core/       Game, GameMode
  database/   Koneksi DB Railway, Query, Model User
  events/     Random event dan threat
  player/     Player dan Inventory
  puzzle/     Puzzle coding beginner friendly
  ui/         Desktop Swing, Login Screen, browser, terminal
  website/    Model browser dan website
```

### 3.3 Alur Permainan

1. Login/Registrasi Akun.
2. Pilih Game Baru atau Load Game.
3. Buka Browser. Selesaikan puzzle Black Lantern, Moth Index, dan Echo Market.
4. Setelah mendapat 3 keys, buka Terminal dan ketik `exit_trace`.
5. Game menyimpan ending status ke Database.

### 3.4 Website Dalam Game

Browser menyediakan 10 website dengan halaman seperti `null://forum.black-lantern` untuk Puzzle Variable Gate.

<!-- MASUKKAN SCREENSHOT DI SINI: Screenshot tampilan dalam game ketika membuka Browser dan menampilkan salah satu kode Puzzle. -->

### 3.5 Perancangan Database

Database menggunakan MySQL/MariaDB yang di-hosting di Railway. Terdiri dari tabel:
- `users` & `user_settings`
- `websites`, `website_body_lines`, `puzzles`, `puzzle_hints` (Data Seed)
- `save_slots`, `save_inventory_items`, `save_solved_puzzles` (Progres Pemain)

<!-- MASUKKAN SCREENSHOT DI SINI: Screenshot dari DBeaver atau HeidiSQL yang menunjukkan tabel-tabel berhasil dibuat di database Railway. -->

## BAB IV IMPLEMENTASI

### 4.1 Class Core & Database
Class `Game` menjadi penghubung utama. `DatabaseManager` bertanggung jawab untuk melakukan koneksi ke Railway menggunakan `jdbc:mariadb://...` yang dibaca dari file `.env`. Ia juga menangani `hashPassword` menggunakan SHA-256.

### 4.2 Class Player, UI, dan Asset
`Player` menyimpan status sesi saat ini yang secara reguler disinkronisasikan ke DB. Bagian UI mencakup `LoginPanel`, `StartMenu`, `DesktopUI`, `BrowserWindow`, dan `TerminalWindow`. Asset proyek digenerate secara dinamis di folder `assets/`.

<!-- MASUKKAN SCREENSHOT DI SINI: Screenshot potongan kode DatabaseManager.java pada bagian method getConnection() atau login(). -->

### 4.3 Implementasi 4 Pilar OOP (Pemrograman Berorientasi Objek)

Sebagai pemenuhan capaian mata kuliah PBO, game ini telah mengimplementasikan 4 pilar utama OOP dengan rincian sebagai berikut:

1. **Encapsulation (Pengkapsulan):**
   Melindungi data sensitif dengan menggunakan modifier `private` dan menyediakan akses terkontrol melalui *getter* dan *setter*.
   * *Contoh pada Project:* Pada class `Player` dan `User`, atribut `password_hash` atau `inventory` bersifat private. Data inventori hanya bisa ditambah melalui method `addItem()`, sehingga state player tidak bisa diubah sembarangan dari class luar yang tidak berkepentingan.

2. **Inheritance (Pewarisan):**
   Menurunkan sifat dan behavior dari class induk (Superclass) ke class anak (Subclass) untuk meminimalisir duplikasi kode.
   * *Contoh pada Project:* Class `BrowserWindow` dan `TerminalWindow` melakukan `extends BaseWindow`. Fitur logika drag-and-drop window, rendering title bar, dan fungsi tombol "Close" hanya ditulis satu kali di `BaseWindow`, namun dapat dinikmati oleh semua jendela turunannya.

3. **Polymorphism (Polimorfisme):**
   Kemampuan objek untuk mengambil berbagai bentuk, biasanya diimplementasikan dengan antarmuka (Interface) atau Overriding.
   * *Contoh pada Project:* Penggunaan Interface `Threat`. Game memiliki berbagai macam objek ancaman (seperti `PopupWarning`, `GlitchEvent`, dll). Core game hanya perlu memanggil method `threat.trigger()`, dan setiap objek threat akan mengeksekusi animasi atau behavior yang berbeda-beda tanpa game perlu mengecek tipe objeknya satu per satu dengan `if-else`.

4. **Abstraction (Abstraksi):**
   Menyembunyikan kerumitan proses di latar belakang dan hanya menyajikan fitur-fitur esensial (membentuk *blueprint*).
   * *Contoh pada Project:* Class abstrak `Puzzle`. Class `Puzzle` tidak mengetahui cara memvalidasi spesifik suatu jawaban, ia hanya mendefinisikan kontrak abstract method bernama `checkAnswer()`. Logika implementasi nyatanya diisi (di-*override*) oleh class turunannya seperti `VariablePuzzle` atau `LoopPuzzle`. Contoh lain adalah `DatabaseManager` yang mengabstraksi kerumitan eksekusi text SQL JDBC dari komponen UI.

<!-- MASUKKAN SCREENSHOT DI SINI: Screenshot potongan kode yang memperlihatkan salah satu implementasi OOP, misalnya saat class BrowserWindow.java menggunakan keyword 'extends BaseWindow' (Pewarisan). -->

## BAB V PENGUJIAN

### 5.1 Metode Pengujian
Pengujian dilakukan menggunakan Ant command untuk menjalankan smoke test non-UI:

```bash
java -cp "build/classes:lib/mariadb-java-client-3.3.3.jar" com.phisinthedark.core.Game --smoke-test
```

### 5.2 Hasil Pengujian

```text
Phis in the Dark smoke test
websites=10
savePath=MariaDB jdbc:mariadb://localhost:3306/phis_in_the_dark
site=black_lantern_forum, puzzle=puzzle_variable_gate
...
site=null_search, puzzle=exploration
requiredPuzzles=3
status=OK
```

Berdasarkan hasil tersebut, koneksi ke Database berhasil (melewati fallback `NullPointerException`), data website ter-load sempurna, dan validasi progresi sukses.

<!-- MASUKKAN SCREENSHOT DI SINI: Screenshot jendela Terminal/Command Prompt yang menunjukkan hasil 'status=OK' dari pengujian smoke-test atau saat game berhasil berjalan ("BUILD SUCCESSFUL"). -->

## BAB VI KESIMPULAN DAN SARAN

### 6.1 Kesimpulan
Phis in the Dark berhasil dibuat sebagai game desktop Java Swing dengan penyimpanan Cloud Database MariaDB. Game ini sukses menerapkan konsep OOP, fake desktop, terminal, puzzle edukatif, event horror ringan, sistem registrasi/login, dan multi-user save slot.

### 6.2 Saran
Pengembangan selanjutnya dapat dilakukan dengan:
1. Melakukan manual check UI pada resolusi kecil dan normal.
2. Menambahkan fitur reset password akun (email recovery).
3. Memperkuat perlindungan SSL (saat ini menggunakan mode bypass).
4. Menambahkan packaging final (Installer `.exe` / `.deb`) agar game langsung dapat dimainkan pengguna.
