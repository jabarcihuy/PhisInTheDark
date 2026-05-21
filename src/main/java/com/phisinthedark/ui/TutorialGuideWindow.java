package com.phisinthedark.ui;

import com.phisinthedark.assets.AssetLoader;
import com.phisinthedark.core.Game;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

public class TutorialGuideWindow extends BaseWindow {
    private final Game game;
    private final JLabel stepLabel;
    private final JTextArea detailArea;
    private final JProgressBar progressBar;

    public TutorialGuideWindow(Game game) {
        super("Tutorial Guide", 460, 320);
        this.game = game;
        this.stepLabel = new JLabel("", SwingConstants.LEFT);
        this.detailArea = new JTextArea();
        this.progressBar = new JProgressBar(0, game.getRequiredPuzzleCount());
        buildGuide();
        update();
    }

    private void buildGuide() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(9, 20, 22));
        panel.setBorder(UiTheme.panelBorder(UiTheme.ACCENT));

        JLabel title = new JLabel("MODE TUTORIAL");
        title.setForeground(UiTheme.ACCENT);
        title.setFont(AssetLoader.terminalFont(18f, Font.BOLD));

        stepLabel.setForeground(UiTheme.TEXT);
        stepLabel.setFont(AssetLoader.terminalFont(14f, Font.BOLD));

        UiTheme.styleProgress(progressBar, UiTheme.ACCENT);

        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setOpaque(false);
        detailArea.setForeground(new Color(210, 232, 228));
        detailArea.setFont(AssetLoader.terminalFont(12f, Font.PLAIN));

        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        header.add(progressBar, BorderLayout.SOUTH);

        JPanel body = new JPanel(new BorderLayout(8, 8));
        body.setOpaque(false);
        body.add(stepLabel, BorderLayout.NORTH);
        body.add(detailArea, BorderLayout.CENTER);

        panel.add(header, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);
        content.add(panel, BorderLayout.CENTER);
    }

    public void update() {
        int solved = game.getPlayer().getSolvedPuzzleCount();
        String step;
        String detail;

        if (solved == 0) {
            step = "1. Buka Browser";
            detail = "Pilih Black Lantern Forum. Pada puzzle pertama, gunakan jawaban: ghost";
        } else if (solved == 1) {
            step = "2. Buka Moth Index";
            detail = "Pilih website Moth Index. Pada puzzle kedua, gunakan jawaban: connect hidden_server";
        } else if (solved == 2) {
            step = "3. Buka Echo Market";
            detail = "Pilih website Echo Market. Pada puzzle ketiga, hitung hasil loop dan jawab dengan angka: 8";
        } else {
            step = "4. Selesaikan game";
            detail = "Buka Terminal, lalu ketik: exit_trace";
        }

        stepLabel.setText(step);
        detailArea.setText(detail + "\n\nTidak ada random popup, glitch challenge, atau warning di mode tutorial.");
        progressBar.setValue(Math.min(solved, game.getRequiredPuzzleCount()));
        progressBar.setString(solved + "/" + game.getRequiredPuzzleCount() + " puzzle");
    }
}
