package com.phisinthedark.puzzle;

import java.util.List;

public class LoopPuzzle extends Puzzle {
    public LoopPuzzle() {
        super(
                "puzzle_signal_doubler",
                "Signal Doubler",
                "loop + logic",
                "signal = 1\n\nfor step in range(3):\n    signal = signal * 2\n\nprint(signal)",
                "Ketik angka saja: berapa nilai signal yang dicetak setelah loop selesai?",
                "8",
                "Halaman terenkripsi retak. Perintah akhir muncul di terminal: exit_trace.",
                "trace_key",
                List.of(
                        "Hint: range(3) berarti loop berjalan tiga kali.",
                        "Hint: mulai dari 1, lalu kalikan 2 setiap putaran.",
                        "Urutannya: 1 -> 2 -> 4 -> 8."
                )
        );
    }

    @Override
    public boolean checkAnswer(String answer) {
        return "8".equals(normalize(answer));
    }
}
