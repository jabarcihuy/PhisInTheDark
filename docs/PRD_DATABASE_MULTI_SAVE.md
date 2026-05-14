# PRD: Phis in the Dark Database, Login, dan Multi Save Slot

## 1. Ringkasan

Phis in the Dark saat ini menyimpan progress pemain di file lokal legacy `~/.nulltrace/save.properties`. Model ini cukup untuk satu pemain, tetapi tidak cukup jika game dimainkan oleh banyak orang atau satu pemain ingin memiliki beberapa progress berbeda.

Fitur database dibuat untuk memindahkan data user, save slot, progress puzzle, inventory, setting, dan riwayat game ke MariaDB. Pada versi pertama, database difokuskan untuk game desktop Java Swing dengan login lokal dan multi save slot.

## 2. Tujuan

- Membedakan progress antar pemain.
- Mendukung satu user memiliki lebih dari satu save slot.
- Menyimpan progress puzzle, inventory key, paranoia, dan ending status per save slot.
- Menyediakan dasar yang rapi untuk fitur lanjutan seperti profile, route archive, analytics, dan admin konten.
- Mengurangi ketergantungan pada satu file save global.

## 3. Non-Tujuan

- Versi pertama tidak wajib mendukung cloud save lintas komputer.
- Versi pertama tidak wajib punya admin panel.
- Versi pertama tidak wajib memindahkan semua konten website dan puzzle ke database.
- Versi pertama tidak wajib punya leaderboard online.
- Versi pertama tidak membahas deployment server publik.

## 4. Pengguna

### Player

Pemain yang membuka Phis in the Dark, login atau memilih akun, lalu memainkan game dengan save slot miliknya sendiri.

Kebutuhan utama:
- Bisa membuat akun.
- Bisa login.
- Bisa membuat save slot baru.
- Bisa load save miliknya sendiri.
- Tidak melihat save milik user lain.

### Developer

Pengembang game yang butuh struktur data jelas untuk menyimpan progress dan menambah fitur di masa depan.

Kebutuhan utama:
- Struktur database mudah dipetakan ke class Java.
- Progress bisa di-debug dari database.
- Data penting tidak tersimpan sebagai string campur aduk.

## 5. Scope Versi Pertama

Versi pertama mencakup:
- Login sederhana dengan username dan password.
- Register user baru.
- Multi save slot per user.
- Save dan load progress normal mode.
- Penyimpanan inventory item.
- Penyimpanan solved puzzle.
- Penyimpanan setting user.
- Penyimpanan status ending.
- Optional browser history dan event log untuk debug.

Versi pertama tidak menyimpan tutorial sebagai progress permanen. Tutorial tetap bisa dimulai ulang dari awal.

## 6. User Flow

### 6.1 Register

1. User membuka game.
2. User memilih `Register`.
3. User mengisi username, display name, dan password.
4. Game membuat user baru.
5. User masuk ke halaman save slot.

### 6.2 Login

1. User membuka game.
2. User memilih `Login`.
3. User mengisi username dan password.
4. Game memvalidasi password.
5. Jika valid, game menampilkan save slot milik user tersebut.
6. Jika tidak valid, game menampilkan pesan gagal login.

### 6.3 New Game

1. User login.
2. User memilih `New Save Slot`.
3. User mengisi nama slot atau memakai nama default.
4. Game membuat save slot kosong.
5. Game masuk ke desktop Phis in the Dark normal mode.

### 6.4 Load Game

1. User login.
2. Game menampilkan daftar save slot milik user.
3. User memilih save slot.
4. Game memuat inventory, solved puzzle, paranoia, dan ending status.
5. Game masuk ke desktop Phis in the Dark sesuai progress slot tersebut.

### 6.5 Save Game

1. User sedang bermain di normal mode.
2. User klik `Save` atau game autosave setelah puzzle selesai.
3. Game menyimpan progress ke save slot aktif.
4. Game memperbarui `updated_at` dan `last_played_at`.

### 6.6 Tutorial

1. User memilih `Tutorial`.
2. Tutorial dimulai dari awal.
3. Tutorial tidak disimpan ke save slot permanen.
4. Save dan load disembunyikan selama tutorial.

## 7. Aturan Produk

- Satu user bisa memiliki banyak save slot.
- Setiap save slot hanya dimiliki oleh satu user.
- User hanya boleh melihat dan membuka save slot miliknya sendiri.
- Password tidak boleh disimpan sebagai plaintext.
- Password harus disimpan sebagai hash.
- Save slot normal menyimpan progress game utama.
- Tutorial tidak menggunakan save slot permanen pada versi pertama.
- Save slot yang sudah mencapai ending tetap bisa dibuka sebagai archive.
- Save slot yang sudah mencapai ending boleh tetap ada, tidak otomatis dihapus.
- Inventory dan solved puzzle disimpan per save slot, bukan per user.
- Setting audio dan preferensi UI disimpan per user, bukan per save slot.

## 8. Data Model

### 8.1 `users`

Menyimpan akun pemain.

Kolom:
- `id`
- `username`
- `password_hash`
- `display_name`
- `created_at`
- `last_login_at`

Catatan:
- `username` harus unik.
- `password_hash` menyimpan hasil hash password, bukan password asli.

### 8.2 `save_slots`

Menyimpan satu progress game milik user.

Kolom:
- `id`
- `user_id`
- `slot_name`
- `mode`
- `paranoia`
- `ending_reached`
- `created_at`
- `updated_at`
- `last_played_at`

Catatan:
- `user_id` mengarah ke user pemilik save.
- `mode` minimal mendukung `NORMAL`.
- `ending_reached` menandakan save sudah masuk route archive.

### 8.3 `websites`

Menyimpan master data website dalam browser game.

Kolom:
- `id`
- `title`
- `url`
- `visual_style`
- `subtitle`
- `screenshot_asset`
- `unlocked_file_title`
- `unlocked_file_content`
- `sort_order`

Catatan:
- Pada versi pertama, tabel ini boleh belum dipakai jika website masih hardcoded di Java.
- Jika dipakai, urutan website ditentukan oleh `sort_order`.

### 8.4 `website_body_lines`

Menyimpan isi teks website yang terdiri dari banyak baris.

Kolom:
- `id`
- `website_id`
- `line_order`
- `content`

Catatan:
- Satu website bisa memiliki banyak body line.
- `line_order` menentukan urutan tampil di browser.

### 8.5 `puzzles`

Menyimpan master data puzzle.

Kolom:
- `id`
- `website_id`
- `title`
- `concept`
- `code_block`
- `instruction`
- `answer`
- `tutorial_answer`
- `reward_item`
- `solved_message`
- `sort_order`

Catatan:
- Tidak semua website memiliki puzzle.
- Pada versi pertama, puzzle juga boleh tetap hardcoded di Java.
- Jika dipindahkan ke database, validasi jawaban tetap harus jelas dan tidak terlalu longgar.

### 8.6 `save_inventory_items`

Menyimpan item yang dimiliki oleh save slot.

Kolom:
- `save_slot_id`
- `item_name`
- `obtained_at`

Contoh item:
- `forum_key`
- `server_route`
- `trace_key`

Catatan:
- Item disimpan per save slot.
- Item yang sama tidak boleh dobel dalam satu save slot.

### 8.7 `save_solved_puzzles`

Menyimpan puzzle yang sudah diselesaikan di save slot tertentu.

Kolom:
- `save_slot_id`
- `puzzle_id`
- `solved_at`
- `attempt_count`
- `last_answer`

Catatan:
- Puzzle yang sama tidak boleh dobel dalam satu save slot.
- `attempt_count` berguna untuk statistik dan balancing puzzle.
- `last_answer` opsional, bisa dikosongkan jika tidak ingin menyimpan input pemain.

### 8.8 `save_browser_history`

Menyimpan riwayat website yang dibuka pemain.

Kolom:
- `id`
- `save_slot_id`
- `website_id`
- `visited_at`

Catatan:
- Tabel ini opsional untuk versi pertama.
- Berguna untuk analytics, debug, dan fitur history browser.

### 8.9 `save_event_logs`

Menyimpan event horror/random yang terjadi selama bermain.

Kolom:
- `id`
- `save_slot_id`
- `event_type`
- `message`
- `paranoia_after`
- `created_at`

Catatan:
- Tabel ini opsional untuk versi pertama.
- Berguna untuk debug event system.

### 8.10 `user_settings`

Menyimpan preferensi user.

Kolom:
- `user_id`
- `audio_muted`
- `ambience_muted`
- `text_speed`
- `fullscreen`
- `updated_at`

Catatan:
- Setting melekat ke user, bukan save slot.
- Jika user membuka save slot berbeda, setting tetap sama.

## 9. Relasi Utama

- `users` ke `save_slots`: satu user punya banyak save slot.
- `save_slots` ke `save_inventory_items`: satu save slot punya banyak item.
- `save_slots` ke `save_solved_puzzles`: satu save slot punya banyak solved puzzle.
- `save_slots` ke `save_browser_history`: satu save slot punya banyak history.
- `save_slots` ke `save_event_logs`: satu save slot punya banyak event log.
- `users` ke `user_settings`: satu user punya satu setting.
- `websites` ke `website_body_lines`: satu website punya banyak body line.
- `websites` ke `puzzles`: satu website punya nol atau satu puzzle.

## 10. Save Data Minimum

Agar save/load normal mode bekerja, data minimum yang wajib disimpan adalah:
- user aktif
- save slot aktif
- paranoia
- ending status
- inventory item
- solved puzzle

Data yang tidak wajib untuk versi pertama:
- browser history
- event log
- attempt count
- last answer
- website master data
- puzzle master data

## 11. Perubahan UI

### Start Menu Baru

Start menu perlu berubah dari:
- Game Baru
- Load Game
- Tutorial

Menjadi:
- Login
- Register
- Exit

Setelah login, tampil:
- New Save Slot
- Load Save Slot
- Tutorial
- Settings
- Logout

### Save Slot Screen

Setiap save slot menampilkan:
- nama slot
- progress key
- status route
- last played
- tombol load
- tombol delete atau reset

Contoh tampilan data:
- `Slot 1 - 2/3 keys - Last played: 2026-05-14`
- `Archive Route - Complete`

## 12. Perubahan Java Class

Class baru yang disarankan:
- `DatabaseManager`
- `User`
- `UserRepository`
- `SaveSlot`
- `SaveSlotRepository`
- `DatabaseSaveManager`
- `LoginWindow`
- `SaveSlotWindow`

Class yang perlu diubah:
- `Game`
- `SaveManager`
- `StartMenu`
- `DesktopUI`
- `Player`

Catatan:
- `SaveManager` lama bisa tetap ada untuk fallback file save.
- `DatabaseSaveManager` menangani MariaDB.
- `Game` perlu menyimpan `currentUser` dan `currentSaveSlot`.

## 13. Keamanan

- Password tidak boleh disimpan plaintext.
- Login gagal tidak boleh memberi tahu apakah username atau password yang salah secara terlalu detail.
- Query database harus memakai prepared statement.
- User hanya boleh load save slot miliknya sendiri.
- Delete save slot harus memakai konfirmasi.

## 14. Acceptance Criteria

Fitur dianggap selesai jika:
- User bisa register.
- User bisa login.
- User bisa logout.
- User bisa membuat save slot baru.
- User bisa melihat daftar save slot miliknya.
- User tidak bisa melihat save slot user lain.
- User bisa load save slot.
- Progress puzzle tersimpan setelah puzzle selesai.
- Inventory tersimpan setelah mendapat reward item.
- Ending status tersimpan setelah `exit_trace`.
- Save archive bisa dibuka lagi.
- Tutorial tetap bisa dimainkan tanpa save permanen.
- Game tetap bisa berjalan jika database tersedia dan konfigurasi benar.

## 15. Risiko

- Database menambah kompleksitas untuk game desktop.
- Koneksi MariaDB bisa gagal jika service database belum berjalan.
- Packaging game menjadi lebih rumit karena butuh JDBC driver.
- Login lokal bisa terasa berlebihan jika game hanya dipakai sendiri.
- Migrasi dari file save lama perlu dirancang jika sudah ada banyak save lama.

## 16. Rencana Implementasi Bertahap

### Tahap 1: Struktur Database dan Repository

- Tambah koneksi MariaDB.
- Tambah model `User` dan `SaveSlot`.
- Tambah repository untuk user dan save slot.
- Tambah register dan login basic.

### Tahap 2: Multi Save Slot

- Tambah layar save slot.
- Tambah create/load/delete save slot.
- Hubungkan save slot dengan `Game`.

### Tahap 3: Save Progress ke Database

- Simpan inventory item.
- Simpan solved puzzle.
- Simpan paranoia dan ending status.
- Ganti save/load normal dari file ke database.

### Tahap 4: Polish dan Fallback

- Tambah pesan error database.
- Tambah fallback jika database mati.
- Tambah migrasi dari file save lama jika dibutuhkan.
- Tambah smoke test database ringan.

## 17. Keputusan Awal yang Direkomendasikan

- Gunakan login sederhana, bukan hanya profile name.
- Satu user boleh punya banyak save slot.
- Tutorial tidak disimpan permanen.
- Website dan puzzle tetap hardcoded dulu di Java untuk versi awal database.
- Database versi pertama fokus pada user, save slot, inventory, solved puzzle, dan setting.
- Browser history dan event logs dibuat opsional.
