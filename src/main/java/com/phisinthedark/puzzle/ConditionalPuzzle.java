package com.phisinthedark.puzzle;

import java.util.List;

public class ConditionalPuzzle extends Puzzle {
    public ConditionalPuzzle() {
        super(
                "puzzle_hidden_server",
                "Cold Signal",
                "if else + command",
                "signal = \"cold\"\n\nif signal == \"cold\":\n    connect hidden_server\nelse:\n    print(\"NO ROUTE\")",
                "Ketik command persis yang dijalankan saat signal bernilai cold.",
                "connect hidden_server",
                "connect hidden_server",
                "Hidden server merespons dengan satu alamat email yang tidak pernah dikirim.",
                "server_route",
                List.of(
                        "Hint: jalur if aktif karena signal sama dengan cold.",
                        "Hint: perintahnya dimulai dengan connect.",
                        "Jawaban persis: connect hidden_server"
                )
        );
    }

    @Override
    public boolean checkAnswer(String answer) {
        return "connect hidden_server".equals(normalize(answer));
    }
}
