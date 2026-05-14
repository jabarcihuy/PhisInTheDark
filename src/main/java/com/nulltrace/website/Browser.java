package com.nulltrace.website;

import com.nulltrace.puzzle.ConditionalPuzzle;
import com.nulltrace.puzzle.LoopPuzzle;
import com.nulltrace.puzzle.VariablePuzzle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Browser {
    private final List<Website> websites;
    private final List<String> history;

    public Browser(List<Website> websites) {
        this.websites = new ArrayList<>(websites);
        this.history = new ArrayList<>();
    }

    public static Browser createDefaultBrowser() {
        List<Website> defaultSites = List.of(
                new Website(
                        "black_lantern_forum",
                        "Black Lantern Forum",
                        "null://forum.black-lantern",
                        Website.VisualStyle.FORUM,
                        "old thread archive // last active: 03:13",
                        "website_hacker_forum.png",
                        List.of(
                                "[USER_13] jangan percaya pop-up yang tahu namamu.",
                                "[root_mirror] akses cuma terbuka kalau variable-nya cocok.",
                                "[guest] aku cuma ingin belajar kenapa komputer ini bicara balik."
                        ),
                        new VariablePuzzle(),
                        "thread_13.txt",
                        "THREAD_13\nKey ditemukan: forum_key\nCatatan: password bukan ditebak. Password dibaca dari variable."
                ),
                new Website(
                        "moth_index",
                        "Moth Index",
                        "null://hidden.moth-index",
                        Website.VisualStyle.WIKI,
                        "hidden wiki // index yang selalu berubah",
                        "website_hidden_wiki.png",
                        List.of(
                                "Artikel 91: If memilih satu jalan dari dua kemungkinan.",
                                "Artikel 92: Server dingin hanya menerima route yang tepat.",
                                "Artikel 93: Tidak semua pesan perlu dibalas."
                        ),
                        new ConditionalPuzzle(),
                        "unsent_mail.eml",
                        "FROM: unknown_user\nTO: you\nSUBJECT: route\n\nKalau signal terasa cold, route-nya bukan halaman. Route-nya perintah."
                ),
                new Website(
                        "echo_market",
                        "Echo Market",
                        "null://market.echo-corrupt",
                        Website.VisualStyle.MARKET,
                        "corrupted marketplace // values repeat until they change",
                        "website_creepy_marketplace.png",
                        List.of(
                                "Item #000: Signal starts at 1.",
                                "Item #013: Every loop doubles the signal.",
                                "Notice: Sistem hanya menerima angka akhir."
                        ),
                        new LoopPuzzle(),
                        "exit_command.note",
                        "EXIT COMMAND\nTerminal menerima perintah akhir setelah tiga key terkumpul:\nexit_trace"
                ),
                new Website(
                        "cold_login",
                        "Encrypted Login",
                        "null://gate.cold-login",
                        Website.VisualStyle.ENCRYPTED,
                        "login page // intentionally incomplete",
                        "website_encrypted_login.png",
                        List.of(
                                "Username field menolak semua nama kecuali yang belum kamu tahu.",
                                "Password field berkedip saat popup muncul.",
                                "Catatan belajar: login adalah contoh comparison yang gagal atau berhasil."
                        ),
                        null,
                        "login_cache.txt",
                        "LOGIN CACHE\nHalaman ini hanya clue visual. Puzzle utama tetap ada di 3 website pertama."
                ),
                new Website(
                        "room_013",
                        "Room 0x13",
                        "null://chat.room-013",
                        Website.VisualStyle.CHAT,
                        "abandoned chatroom // one user remains",
                        "website_abandoned_chatroom.png",
                        List.of(
                                "03:12 <mira> kamu dengar static?",
                                "03:13 <null> jangan ketik namamu di terminal.",
                                "03:14 <you> pesan ini tidak pernah kamu kirim."
                        ),
                        null,
                        "chat_log_013.txt",
                        "ROOM 0X13\nTidak ada puzzle di sini. Hanya atmosfer dan clue."
                ),
                new Website(
                        "broken_mirror",
                        "Broken Mirror",
                        "null://mirror.corrupted",
                        Website.VisualStyle.CORRUPTED,
                        "corrupted website // render unstable",
                        "website_corrupted_website.png",
                        List.of(
                                "ERR_RENDER_451",
                                "Body remembers previous input.",
                                "Jika browser glitch, jangan panik. Itu hanya event ringan."
                        ),
                        null,
                        "mirror_error.log",
                        "MIRROR ERROR\nVisual glitch bukan kegagalan game. Itu bagian dari atmosfer."
                ),
                new Website(
                        "thirteen_notes",
                        "Thirteen Notes",
                        "null://blog.thirteen-notes",
                        Website.VisualStyle.BLOG,
                        "mysterious blog // beginner logic diary",
                        "website_mysterious_blog.png",
                        List.of(
                                "Post: variable adalah kotak bernama.",
                                "Post: if else memilih jalan.",
                                "Post: loop mengulang aksi kecil sampai cukup."
                        ),
                        null,
                        "blog_note.txt",
                        "BLOG NOTE\nBelajar coding lebih ringan ketika setiap konsep menjadi aksi kecil."
                ),
                new Website(
                        "archive_404",
                        "Archive 404",
                        "null://gov.archive-404",
                        Website.VisualStyle.ARCHIVE,
                        "fake government archive // redacted",
                        "website_government_archive.png",
                        List.of(
                                "Document NT-001: redacted.",
                                "Case label: amateur signal intrusion.",
                                "Educational exception granted."
                        ),
                        null,
                        "archive_404.txt",
                        "ARCHIVE 404\nFile ini palsu. Namun struktur log dapat dibaca seperti data."
                ),
                new Website(
                        "wire_room",
                        "Wire Room",
                        "null://forum.wire-room",
                        Website.VisualStyle.CONSPIRACY,
                        "conspiracy forum // too many theories",
                        "website_conspiracy_forum.png",
                        List.of(
                                "Theory: popup bukan bug, tapi pesan.",
                                "Proof: wallpaper berubah setelah puzzle selesai.",
                                "Thread locked by moderator yang tidak pernah login."
                        ),
                        null,
                        "wire_room.thread",
                        "WIRE ROOM\nTeori paling berguna: selesaikan puzzle, baca reward file."
                ),
                new Website(
                        "null_search",
                        "Null Search",
                        "null://search.deep-null",
                        Website.VisualStyle.SEARCH,
                        "deep web search page // results drift",
                        "website_deep_web_search.png",
                        List.of(
                                "Result 1: Black Lantern Forum.",
                                "Result 2: Moth Index.",
                                "Result 3: Echo Market.",
                                "Result 4: halaman yang belum ingin ditemukan."
                        ),
                        null,
                        "search_results.cache",
                        "NULL SEARCH\nTiga result pertama adalah jalur utama menuju ending."
                )
        );
        return new Browser(defaultSites);
    }

    public void visit(Website website) {
        if (website != null) {
            history.add(website.getUrl());
        }
    }

    public Optional<Website> findById(String id) {
        return websites.stream()
                .filter(website -> website.getId().equals(id))
                .findFirst();
    }

    public List<Website> getWebsites() {
        return Collections.unmodifiableList(websites);
    }

    public List<String> getHistory() {
        return Collections.unmodifiableList(history);
    }
}
