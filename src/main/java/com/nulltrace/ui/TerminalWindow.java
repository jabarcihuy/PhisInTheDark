package com.nulltrace.ui;

import com.nulltrace.assets.AssetLoader;
import com.nulltrace.core.Game;
import com.nulltrace.website.Website;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TerminalWindow extends BaseWindow {
    private final Game game;
    private final DesktopUI desktopUI;
    private final JTextArea output;
    private final JTextField input;
    private final List<String> commandHistory;
    private int historyIndex;

    public TerminalWindow(Game game, DesktopUI desktopUI) {
        super("Terminal", 680, 430);
        this.game = game;
        this.desktopUI = desktopUI;
        this.output = new JTextArea() {
            private final BufferedImage background = AssetLoader.loadImage("terminal_background.png");

            @Override
            protected void paintComponent(Graphics graphics) {
                if (background != null) {
                    graphics.drawImage(background, 0, 0, getWidth(), getHeight(), null);
                }
                super.paintComponent(graphics);
            }
        };
        this.input = new JTextField();
        this.commandHistory = new ArrayList<>();
        this.historyIndex = 0;

        buildTerminal();
        printWelcome();
    }

    private void buildTerminal() {
        output.setEditable(false);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        output.setFont(AssetLoader.terminalFont(13f, Font.PLAIN));
        output.setBackground(new Color(4, 7, 8));
        output.setOpaque(false);
        output.setForeground(new Color(198, 244, 230));
        output.setCaretColor(ACCENT);
        output.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        UiTheme.styleTextComponent(input, UiTheme.ACCENT);
        input.addActionListener(event -> runCommandFromInput());
        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_UP) {
                    navigateHistory(-1);
                    event.consume();
                } else if (event.getKeyCode() == KeyEvent.VK_DOWN) {
                    navigateHistory(1);
                    event.consume();
                }
            }
        });

        JPanel bottom = new JPanel(new BorderLayout(6, 0));
        bottom.setOpaque(false);
        JLabel prompt = new JLabel("null@trace:~$ ");
        prompt.setForeground(ACCENT);
        prompt.setFont(AssetLoader.terminalFont(13f, Font.BOLD));
        JButton run = new JButton("Run");
        UiTheme.styleButton(run, UiTheme.ACCENT);
        run.addActionListener(event -> runCommandFromInput());
        desktopUI.applyButtonHover(run);
        bottom.add(prompt, BorderLayout.WEST);
        bottom.add(input, BorderLayout.CENTER);
        bottom.add(run, BorderLayout.EAST);

        JPanel commandBar = new JPanel(new GridLayout(1, 5, 6, 0));
        commandBar.setOpaque(false);
        commandBar.add(commandButton("help"));
        commandBar.add(commandButton("browser"));
        commandBar.add(commandButton("sites"));
        commandBar.add(commandButton("status"));
        commandBar.add(commandButton("decrypt"));

        JScrollPane outputScroll = new JScrollPane(output);
        outputScroll.setBorder(BorderFactory.createLineBorder(new Color(31, 49, 54), 1));
        outputScroll.getViewport().setOpaque(false);

        content.add(commandBar, BorderLayout.NORTH);
        content.add(outputScroll, BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);
    }

    private void printWelcome() {
        if (game.isTutorialMode()) {
            appendLine("Phis in the Dark tutorial terminal ready.");
            appendLine("Ikuti Tutorial Guide. Ketik help jika perlu daftar command.");
        } else {
            appendLine("Phis in the Dark terminal ready.");
            appendLine("Type help for beginner commands.");
        }
        appendLine("");
    }

    private void handleCommand(String rawCommand) {
        String command = rawCommand.trim();
        if (command.isEmpty()) {
            return;
        }

        game.getAudioManager().playTyping();
        appendLine("null@trace:~$ " + command);
        rememberCommand(command);

        switch (command.toLowerCase(Locale.ROOT)) {
            case "help" -> printHelp();
            case "scan" -> printScan();
            case "sites" -> printSites();
            case "status" -> printStatus();
            case "notes" -> {
                appendLine("opening notes.txt...");
                desktopUI.openNotes();
            }
            case "mail" -> {
                appendLine("opening mailbox...");
                desktopUI.openEmail();
            }
            case "browser" -> {
                appendLine("opening browser...");
                desktopUI.launchBrowser();
            }
            case "save" -> appendLine(game.isTutorialMode() ? "tutorial route has no save." : game.saveGame() ? "saved." : "save failed.");
            case "load" -> appendLine(game.isTutorialMode() ? "tutorial route always starts from step 1." : game.loadGame() ? "loaded." : "no save found.");
            case "decrypt" -> runDecryptAnimation();
            case "clear" -> output.setText("");
            case "exit_trace" -> {
                appendLine("closing route...");
                game.finishGame();
            }
            default -> handleUnknown(command);
        }
    }

    private void printHelp() {
        appendLine("commands:");
        appendLine("  browser   open fake browser");
        appendLine("  sites     list websites");
        appendLine("  scan      scan solved keys");
        appendLine("  notes     open notes");
        appendLine("  mail      open fake email");
        appendLine("  decrypt   run simple decrypt animation");
        if (!game.isTutorialMode()) {
            appendLine("  save      save progress");
            appendLine("  load      load progress");
        }
        appendLine("  status    show current progress");
        appendLine("  exit_trace ending command after 3 puzzles");
        if (game.isTutorialMode()) {
            appendLine("");
            appendLine("tutorial route:");
            appendLine("  1. browser");
            appendLine("  2. solve three guided puzzles");
            appendLine("  3. exit_trace");
        }
        appendLine("");
    }

    private void printSites() {
        appendLine("known addresses:");
        for (Website website : game.getBrowser().getWebsites()) {
            appendLine("  " + website.getUrl() + " - " + website.getTitle());
        }
        appendLine("");
    }

    private void printScan() {
        appendLine("inventory scan:");
        if (game.getPlayer().getInventory().getItems().isEmpty()) {
            appendLine("  no keys yet.");
        } else {
            for (String item : game.getPlayer().getInventory().getItems()) {
                appendLine("  " + item);
            }
        }
        appendLine("");
    }

    public void printStatus() {
        appendLine("progress: " + game.getPlayer().getSolvedPuzzleCount() + "/" + game.getRequiredPuzzleCount() + " puzzles solved.");
        appendLine("next: " + currentObjective());
        if (game.isTutorialMode()) {
            appendLine("mode: tutorial, random warning disabled.");
        } else {
            appendLine("paranoia: " + game.getPlayer().getParanoia() + "/100");
        }
        appendLine("");
    }

    private void runDecryptAnimation() {
        input.setEnabled(false);
        appendLine("decrypt start");
        int[] step = {0};
        Timer timer = new Timer(240, null);
        timer.addActionListener(event -> {
            step[0]++;
            appendLine("  block " + step[0] + "/5 " + "#".repeat(step[0]));
            if (step[0] >= 5) {
                timer.stop();
                input.setEnabled(true);
                input.requestFocusInWindow();
                if (game.canFinishGame()) {
                    appendLine("decrypt complete: exit_trace available.");
                } else if (game.isTutorialMode()) {
                    appendLine("tutorial: lanjutkan puzzle yang ditunjukkan oleh Tutorial Guide.");
                } else {
                    appendLine("decrypt incomplete: collect 3 keys.");
                }
                appendLine("");
            }
        });
        timer.start();
    }

    private void handleUnknown(String command) {
        if (game.isTutorialMode()) {
            appendLine("tutorial: command belum diperlukan. Ikuti Tutorial Guide atau ketik help.");
            appendLine("");
            return;
        }
        if (command.contains("hidden_server")) {
            appendLine("route accepted by website puzzle, not terminal. Open Moth Index.");
        } else if (command.contains("ghost")) {
            appendLine("ghost is a value. Try it inside the browser puzzle.");
        } else if (command.equals("8") || command.contains("signal")) {
            appendLine("signal answers belong inside the browser puzzle.");
        } else {
            appendLine("unknown command. Type help.");
        }
        appendLine("");
    }

    private void appendLine(String line) {
        output.append(line + "\n");
        output.setCaretPosition(output.getDocument().getLength());
    }

    private JButton commandButton(String command) {
        JButton button = new JButton(command);
        UiTheme.styleToolButton(button);
        button.addActionListener(event -> handleCommand(command));
        desktopUI.applyButtonHover(button);
        return button;
    }

    private void runCommandFromInput() {
        String command = input.getText();
        input.setText("");
        handleCommand(command);
    }

    private void rememberCommand(String command) {
        if (commandHistory.isEmpty() || !commandHistory.get(commandHistory.size() - 1).equals(command)) {
            commandHistory.add(command);
        }
        historyIndex = commandHistory.size();
    }

    private void navigateHistory(int direction) {
        if (commandHistory.isEmpty()) {
            return;
        }

        historyIndex = Math.max(0, Math.min(commandHistory.size(), historyIndex + direction));
        if (historyIndex == commandHistory.size()) {
            input.setText("");
        } else {
            input.setText(commandHistory.get(historyIndex));
            input.setCaretPosition(input.getText().length());
        }
    }

    private String currentObjective() {
        if (game.getPlayer().isEndingReached()) {
            return "route archived";
        }
        if (game.canFinishGame()) {
            return "type exit_trace";
        }
        if (!game.getPlayer().getInventory().hasItem("forum_key")) {
            return "open Black Lantern Forum";
        }
        if (!game.getPlayer().getInventory().hasItem("server_route")) {
            return "open Moth Index";
        }
        return "open Echo Market";
    }
}
