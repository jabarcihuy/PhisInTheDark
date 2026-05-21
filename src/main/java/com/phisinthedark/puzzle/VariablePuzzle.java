package com.phisinthedark.puzzle;

import java.util.List;

public class VariablePuzzle extends Puzzle {
    public VariablePuzzle() {
        super(
                "puzzle_variable_gate",
                "Variable Gate",
                "variable + comparison",
                "password = \"ghost\"\n\nif password == \"_____\":\n    print(\"ACCESS GRANTED\")",
                "Ketik nilai string yang tepat untuk mengisi bagian kosong. Jawaban harus satu kata.",
                "ghost",
                "Forum membuka thread lama: USER_13 meninggalkan key pertama.",
                "forum_key",
                List.of(
                        "Hint: lihat isi variable password.",
                        "Hint: password berisi kata ghost.",
                        "Jawaban paling pendek: ghost"
                )
        );
    }

    @Override
    public boolean checkAnswer(String answer) {
        return "ghost".equals(normalize(answer));
    }
}
