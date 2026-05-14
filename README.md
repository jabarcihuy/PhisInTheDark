# Phis in the Dark

Phis in the Dark adalah game desktop full Java Swing bertema psychological horror hacking simulator. Versi ini adalah playable prototype yang ringan, beginner friendly, dan fokus pada logic programming dasar.

Status progress saat ini: sekitar **68%** menuju versi full Java desktop game yang matang. Core gameplay sudah berjalan, tetapi masih perlu polish visual manual, variasi konten, ending yang lebih kuat, dan packaging final.

## Cara Run

Dengan Maven:

```bash
mvn clean package
mvn exec:java
```

Atau jalankan manual jika Maven belum tersedia:

```bash
javac -d out $(find src/main/java -name "*.java")
java -cp out com.nulltrace.core.Game
```

Regenerate asset dasar:

```bash
java -cp out com.nulltrace.assets.AssetGenerator --force
```

Smoke test tanpa membuka UI:

```bash
java -cp out com.nulltrace.core.Game --smoke-test
```

## Struktur Folder

```text
src/main/java/com/nulltrace/
  audio/      Audio placeholder
  core/       Game dan save manager
  events/     Random event dan threat
  player/     Player dan inventory
  puzzle/     Puzzle coding beginner friendly
  ui/         Desktop Swing, browser, terminal, popup
  website/    Model browser dan website

assets/
  images/
  sounds/
  fonts/
```

## Fitur MVP

- Menu awal dengan pilihan `Game Baru`, `Load Game`, dan `Tutorial`
- Popup konfirmasi custom untuk setiap navigasi menu utama
- Status save normal tampil di menu utama dan tombol `Load Game`
- Fake desktop dengan taskbar berkedip
- Taskbar dengan mode, jumlah key, dan objective berikutnya
- Browser window dengan 10 website berbeda
- Browser dropdown dengan indikator `[NEXT]`, `[SOLVED]`, `[LOCKED]`, dan `[INFO]`
- 10 fake website page asset dengan visual berbeda
- Terminal window dengan command dasar
- 3 coding puzzle logic pendek dengan jawaban utama yang pasti
- Popup random dan notification, dengan batas maksimal 2 popup `UNKNOWN USER` aktif
- Browser glitch ringan
- Wallpaper corrupt sederhana
- Fake loading screen
- Notes dengan objective dan key checklist
- Fake email progresif sesuai key yang sudah didapat
- File reward setelah puzzle selesai
- Settings window untuk mute/unmute audio, reset save normal, dan kembali ke menu utama
- Generated PNG/WAV/font asset di `assets/images`, `assets/sounds`, dan `assets/fonts`
- Save/load sederhana di legacy path `~/.nulltrace/save.properties`
- Completed save handling: save yang sudah tamat dibuka sebagai route archive
- 1 ending via command `exit_trace`, lalu kembali ke menu utama

## Controls

- Pada menu awal, pilih `Game Baru`, `Load Game`, atau `Tutorial`.
- `Tutorial` selalu mulai dari step 1 dan tidak memakai save/load.
- Klik ikon desktop untuk membuka Browser, Terminal, Notes, Mail, Settings, Save, dan Load.
- Pada mode Tutorial, tombol Save dan Load disembunyikan.
- Di browser, pilih website dari dropdown, baca potongan kode, lalu isi jawaban.
- Jika jawaban salah, hint menampilkan attempt dan input diberi highlight warning.
- Di terminal, ketik `help` untuk melihat command.
- Setelah semua puzzle selesai, ketik `exit_trace` di terminal.

## Tutorial Mode

Mode tutorial dibuat untuk pemain baru yang ingin memahami alur tanpa tekanan.

- Random popup, browser glitch event, warning event, dan ambience horror random dimatikan.
- Browser tutorial hanya menampilkan tiga website puzzle utama.
- Panel `Tutorial Guide` menunjukkan langkah berikutnya.
- Setiap puzzle menyediakan tombol `Isi Jawaban Tutorial`.
- Jika jawaban salah, game mengisi jawaban yang benar dan meminta pemain menekan `Submit`.
- Tutorial dibuat sebagai sesi belajar singkat dan selalu dimulai ulang dari step 1.
- Tutorial tidak autosave dan tidak memakai load tutorial.

## Puzzle

Puzzle memakai pseudo language bergaya Python/Bash:

- Variable Gate: variable dan comparison
- Cold Signal: if else dan command
- Signal Doubler: loop dan logic angka

Puzzle sekarang memakai jawaban utama yang lebih pasti:

- Variable Gate menerima `ghost`
- Cold Signal menerima `connect hidden_server`
- Signal Doubler menerima `8`

Smoke test juga mengecek beberapa jawaban salah agar puzzle tidak terlalu longgar.

## Progress

Perkiraan status menuju full Java desktop game matang:

- Struktur Java/OOP: 80%
- Core gameplay loop: 85%
- Swing UI/UX: 65%
- Save/load normal: 75%
- Tutorial mode: 75%
- Puzzle logic: 60%
- Event horror/random threat: 60%
- Asset Java2D/audio generated dari Java: 70%
- Testing/smoke test: 55%
- Polish final: 45%

Total estimasi: **68%**.

## TODO Terdekat

Lihat `TODO.md` untuk backlog terbaru. Saat ini sisa utama adalah visual manual check pada resolusi normal/kecil dan ide lanjutan seperti reset current run serta toggle ambience terpisah.

## OOP Yang Digunakan

- Encapsulation: field private dan getter/setter di `Player`, `Inventory`, `Website`, `Puzzle`
- Inheritance: `BrowserWindow`, `TerminalWindow`, dan `TextWindow` mewarisi `BaseWindow`
- Polymorphism: daftar `Threat` menjalankan event berbeda lewat interface yang sama
- Abstraction: `Puzzle` dan `AbstractThreat` menjadi base behavior
- Interface: `Solvable` dan `Threat`

## Catatan Development

Project sengaja tidak memakai framework kompleks atau dependency tambahan. Asset dasar digenerate dengan Java2D dan Java Sound supaya game langsung punya visual/audio ringan tanpa download tambahan.
