# Phis in the Dark

Phis in the Dark adalah game desktop full Java Swing bertema psychological horror hacking simulator. Versi ini adalah playable prototype yang ringan, beginner friendly, dan fokus pada logic programming dasar.

Game ini telah berevolusi dan sekarang menggunakan **Cloud Database (MariaDB/MySQL)** via Railway untuk menyimpan data multi-user (Login/Register), Save Slots, Inventory, dan Puzzle yang telah diselesaikan.

Status progress saat ini: sekitar **85%** menuju versi full Java desktop game yang matang. Core gameplay, sistem autentikasi, save-state database, dan loop permainan sudah berjalan.

## Prasyarat (Requirements)

- **Java JDK 17** atau lebih baru.
- **Koneksi Internet** (Dibutuhkan untuk login dan mengambil save-data dari database Railway).
- Database MariaDB/MySQL.

## Cara Konfigurasi Database (Railway)

Project ini membaca konfigurasi database menggunakan file `.env` melalui bawaan `java.util.Properties`.

1. Buat file baru bernama `.env` di dalam folder root project.
2. Isi file tersebut dengan kredensial database Anda:
   ```env
   DB_URL=jdbc:mariadb://<HOST>:<PORT>/<DATABASE>?useSSL=false
   DB_USER=root
   DB_PASSWORD=password_database_anda
   ```
*(Catatan: Jika file `.env` tidak ditemukan, game akan mencoba terkoneksi ke localhost:3306 secara otomatis).*

## Cara Run

Project ini direkomendasikan untuk dibuka dan di-run menggunakan **Apache NetBeans** (via `build.xml`), tetapi juga mendukung kompilasi terminal.

### 1. Menggunakan NetBeans (Ant)
- Buka project di NetBeans IDE.
- Klik **Clean and Build** (Shift + F11).
- Klik **Run Project** (F6).

### 2. Menggunakan Terminal (Ant Command)
Jika Anda memiliki Apache Ant terinstall di terminal:
```bash
ant compile
ant run
```

*(Catatan: Konfigurasi Maven `pom.xml` juga tersedia sebagai alternatif jika diperlukan).*

---

## Struktur Folder

```text
src/main/java/com/phisinthedark/
  assets/     Asset generator dasar
  audio/      Audio manager dan efek suara
  core/       Game manager utama
  database/   Sistem koneksi DB, Login, Register, dan query (Railway)
  events/     Random event, glitch, dan threat
  player/     Logika Player dan Inventory
  puzzle/     Logika Puzzle, Hint, dan Verifikasi jawaban
  ui/         Komponen Desktop Swing, Login Screen, Browser, Terminal, dll
  website/    Model browser, website page, dan file

assets/
  images/     (Digenerate otomatis jika belum ada)
  sounds/     (Digenerate otomatis jika belum ada)
  fonts/      (Digenerate otomatis jika belum ada)
```

## Fitur MVP & Pembaruan

- **Sistem Akun (Baru):** Login dan Registrasi User yang terhubung langsung ke database Cloud Railway. Data tiap user terpisah dengan aman.
- **Sistem Save Database (Baru):** Proses Load Game dan Save Game tidak lagi menggunakan file lokal, melainkan disimpan secara permanen di database online (termasuk progress puzzle, inventori, dan setting).
- Menu awal dengan pilihan `Game Baru`, `Load Game`, dan `Tutorial`
- Fake desktop dengan taskbar berkedip
- Taskbar dengan mode, jumlah key, dan objective berikutnya
- Browser window dengan website interaktif yang datanya di-*seed* otomatis ke dalam database saat pertama kali jalan.
- Browser dropdown dengan indikator `[NEXT]`, `[SOLVED]`, `[LOCKED]`, dan `[INFO]`
- Terminal window dengan command dasar.
- 3 coding puzzle logic pendek bergaya pseudo-language.
- Popup random dan notification, dengan batas maksimal popup aktif.
- Browser glitch ringan dan Wallpaper corrupt sederhana.
- Notes dengan objective dan key checklist
- Fake email progresif sesuai key yang sudah didapat
- File reward (unlocked files) setelah puzzle selesai
- Settings window untuk mute/unmute audio (tersimpan otomatis per-user di database).

## Controls

- Mulai game dan lakukan **Register** akun baru atau **Login** dengan akun yang sudah ada.
- Pada menu awal, pilih `Game Baru`, `Load Game`, atau `Tutorial`.
- Klik ikon desktop untuk membuka Browser, Terminal, Notes, Mail, Settings, dan aplikasi lainnya.
- Di browser, pilih website dari dropdown, baca potongan kode, lalu isi jawaban.
- Jika jawaban salah, hint menampilkan attempt dan input diberi highlight warning.
- Di terminal, ketik `help` untuk melihat command.
- Setelah semua puzzle selesai, ketik `exit_trace` di terminal.

## Puzzle

Puzzle memakai pseudo language bergaya Python/Bash:

- Variable Gate menerima `ghost`
- Cold Signal menerima `connect hidden_server`
- Signal Doubler menerima `8`

## OOP Yang Digunakan

- **Encapsulation:** Penggunaan getter/setter dan private field di `Player`, `Website`, `User`, dll.
- **Inheritance:** `BrowserWindow`, `TerminalWindow`, dan `TextWindow` mewarisi kelas abstrak dasar seperti `BaseWindow`.
- **Polymorphism:** Daftar `Threat` menjalankan event berbeda lewat satu interface yang sama.
- **Abstraction:** Database dipisahkan menjadi `DatabaseManager`, menyembunyikan kompleksitas query SQL dari UI.

## Catatan Development

Project sengaja tidak memakai library UI framework eksternal untuk melatih pemahaman Java Swing murni. Asset dasar digenerate via Java2D dan Java Sound supaya game langsung memiliki visual/audio tanpa harus mengunduh file besar. Koneksi database kini memanfaatkan JDBC Driver `mariadb-java-client` dan pembacaan `.env` native tanpa library tambahan.
