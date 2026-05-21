package com.phisinthedark.core;

import com.phisinthedark.assets.AssetGenerator;
import com.phisinthedark.assets.AssetLoader;
import com.phisinthedark.audio.AudioManager;
import com.phisinthedark.events.EventSystem;
import com.phisinthedark.player.Player;
import com.phisinthedark.puzzle.Puzzle;
import com.phisinthedark.puzzle.ConditionalPuzzle;
import com.phisinthedark.puzzle.LoopPuzzle;
import com.phisinthedark.puzzle.VariablePuzzle;
import com.phisinthedark.ui.DesktopUI;
import com.phisinthedark.ui.StartMenu;
import com.phisinthedark.ui.UiTheme;
import com.phisinthedark.website.Browser;
import com.phisinthedark.website.Website;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Game {
    private final Player player;
    private final Browser browser;
    private final AudioManager audioManager;
    private final SaveManager saveManager;
    private GameMode mode;
    private DesktopUI desktopUI;
    private EventSystem eventSystem;

    public Game() {
        AssetGenerator.ensureAssets();
        this.player = new Player("student");
        this.browser = Browser.createDefaultBrowser();
        this.audioManager = new AudioManager();
        this.saveManager = new SaveManager();
        this.mode = GameMode.NORMAL;
    }

    public static void main(String[] args) {
        Game game = new Game();
        if (Arrays.asList(args).contains("--smoke-test")) {
            System.out.println(game.runSmokeTest());
            return;
        }

        try {
            SwingUtilities.invokeLater(game::start);
        } catch (Throwable error) {
            System.err.println("Cannot start Phis in the Dark Swing UI: " + error.getMessage());
            System.err.println("Run with --smoke-test to validate non-UI game data.");
        }
    }

    public void start() {
        try {
            UiTheme.applyGlobalDefaults();
            StartMenu menu = new StartMenu(this::startWithChoice, saveManager.describeSave());
            menu.setVisible(true);
        } catch (Throwable error) {
            System.err.println("Cannot start Phis in the Dark Swing UI: " + error.getMessage());
            System.err.println("Run with --smoke-test to validate non-UI game data.");
        }
    }

    private void startWithChoice(StartMenu.StartChoice choice) {
        this.mode = choice.mode();
        player.clearProgress();

        showBootScreen(() -> {
            desktopUI = new DesktopUI(this);
            eventSystem = new EventSystem(desktopUI, player, audioManager);
            desktopUI.setEventSystem(eventSystem);

            boolean loaded = choice.loadSave() && saveManager.load(player, mode);
            desktopUI.refreshAfterLoad();
            desktopUI.setVisible(true);
            if (loaded && player.isEndingReached()) {
                desktopUI.showCompletedRouteNotice();
            } else if (isTutorialMode()) {
                desktopUI.showTutorialGuide();
            } else {
                eventSystem.start();
                audioManager.loopAmbience();
            }

            if (loaded && player.isEndingReached()) {
                desktopUI.showNotification("ROUTE ARCHIVED", "Save ini sudah selesai.");
            } else if (loaded) {
                desktopUI.showNotification("SAVE LOADED", progressSummary());
            } else if (choice.loadSave()) {
                desktopUI.showNotification("NO SAVE", "Save belum ada. Mulai dari awal.");
            } else if (isTutorialMode()) {
                desktopUI.showNotification("TUTORIAL", "Mulai dari step 1. Ikuti panel Tutorial Guide.");
            } else {
                desktopUI.showNotification("GAME BARU", "Buka browser untuk mulai investigasi.");
            }
        });
    }

    private void showBootScreen(Runnable afterBoot) {
        JFrame boot = new JFrame("Phis in the Dark Boot");
        boot.setUndecorated(true);
        boot.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        BufferedImage loadingImage = AssetLoader.loadImage("loading_screen.png");
        JPanel panel = new JPanel(new BorderLayout(8, 8)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                if (loadingImage != null) {
                    graphics.drawImage(loadingImage, 0, 0, getWidth(), getHeight(), null);
                }
            }
        };
        panel.setBackground(UiTheme.BACKDROP);
        panel.setBorder(UiTheme.panelBorder(UiTheme.ACCENT));

        JLabel title = new JLabel("PHIS IN THE DARK", SwingConstants.CENTER);
        title.setForeground(UiTheme.ACCENT);
        title.setFont(UiTheme.font(28f, Font.BOLD));

        JLabel line = new JLabel("mounting fake desktop...", SwingConstants.CENTER);
        line.setForeground(UiTheme.TEXT_MUTED);
        line.setFont(UiTheme.font(13f, Font.PLAIN));

        JProgressBar progress = new JProgressBar(0, 100);
        UiTheme.styleProgress(progress, UiTheme.ACCENT);

        panel.add(title, BorderLayout.NORTH);
        panel.add(line, BorderLayout.CENTER);
        panel.add(progress, BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(430, 150));

        boot.setContentPane(panel);
        boot.pack();
        boot.setLocationRelativeTo(null);
        boot.setVisible(true);

        Timer timer = new Timer(35, null);
        timer.addActionListener(event -> {
            int next = progress.getValue() + 4;
            progress.setValue(next);
            if (next >= 100) {
                timer.stop();
                boot.dispose();
                afterBoot.run();
            }
        });
        timer.start();
    }

    public void puzzleSolved(Website website, Puzzle puzzle) {
        boolean newlySolved = player.solvePuzzle(puzzle.getId());
        if (!newlySolved) {
            return;
        }

        player.getInventory().addItem(puzzle.getRewardItem());
        audioManager.playNotification();
        if (!isTutorialMode()) {
            saveManager.save(player, mode);
        }

        if (desktopUI != null) {
            desktopUI.onPuzzleSolved(website, puzzle);
        }

        if (eventSystem != null && !isTutorialMode()) {
            eventSystem.triggerPuzzleSolvedEvent();
        }
    }

    public boolean saveGame() {
        if (isTutorialMode()) {
            return false;
        }
        return saveManager.save(player, mode);
    }

    public boolean loadGame() {
        if (isTutorialMode()) {
            return false;
        }
        boolean loaded = saveManager.load(player, mode);
        if (desktopUI != null) {
            desktopUI.refreshAfterLoad();
            if (loaded && player.isEndingReached()) {
                if (eventSystem != null) {
                    eventSystem.stop();
                }
                audioManager.stopAmbience();
                desktopUI.showCompletedRouteNotice();
            }
        }
        return loaded;
    }

    public String progressSummary() {
        if (player.isEndingReached()) {
            return "Route complete.";
        }
        if (canFinishGame()) {
            return "3/3 keys. Next: terminal exit_trace.";
        }
        if (!player.getInventory().hasItem("forum_key")) {
            return player.getSolvedPuzzleCount() + "/3 keys. Next: Black Lantern Forum.";
        }
        if (!player.getInventory().hasItem("server_route")) {
            return player.getSolvedPuzzleCount() + "/3 keys. Next: Moth Index.";
        }
        return player.getSolvedPuzzleCount() + "/3 keys. Next: Echo Market.";
    }

    public boolean resetNormalSave() {
        return saveManager.deleteSave();
    }

    public boolean canFinishGame() {
        return player.getSolvedPuzzleCount() >= getRequiredPuzzleCount();
    }

    public boolean isPuzzleUnlocked(Website website) {
        Puzzle puzzle = website.getPuzzle();
        if (puzzle == null || player.hasSolved(puzzle.getId())) {
            return true;
        }
        if (!player.getInventory().hasItem("forum_key")) {
            return "black_lantern_forum".equals(website.getId());
        }
        if (!player.getInventory().hasItem("server_route")) {
            return "moth_index".equals(website.getId());
        }
        if (!player.getInventory().hasItem("trace_key")) {
            return "echo_market".equals(website.getId());
        }
        return false;
    }

    public void finishGame() {
        if (desktopUI == null) {
            return;
        }

        if (!canFinishGame()) {
            if (isTutorialMode()) {
                desktopUI.showNotification("TUTORIAL", "Selesaikan tiga langkah puzzle dulu, lalu ketik exit_trace.");
                desktopUI.showTutorialGuide();
            } else {
                desktopUI.showNotification("TRACE LOCKED", "Selesaikan 3 puzzle dulu.");
                audioManager.playError();
            }
            return;
        }

        if (eventSystem != null) {
            eventSystem.stop();
        }
        audioManager.stopAmbience();
        player.setEndingReached(true);
        if (!isTutorialMode()) {
            saveManager.save(player, mode);
        }
        desktopUI.showEnding();
    }

    public void returnToMainMenu() {
        if (desktopUI == null && eventSystem == null) {
            return;
        }

        if (eventSystem != null) {
            eventSystem.stop();
            eventSystem = null;
        }
        audioManager.stopAmbience();

        if (desktopUI != null) {
            DesktopUI closingDesktop = desktopUI;
            desktopUI = null;
            closingDesktop.dispose();
        }

        SwingUtilities.invokeLater(this::start);
    }

    public int getRequiredPuzzleCount() {
        return (int) browser.getWebsites().stream()
                .filter(website -> website.getPuzzle() != null)
                .count();
    }

    public Player getPlayer() {
        return player;
    }

    public Browser getBrowser() {
        return browser;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public GameMode getMode() {
        return mode;
    }

    public boolean isTutorialMode() {
        return mode == GameMode.TUTORIAL;
    }

    public String runSmokeTest() {
        StringBuilder result = new StringBuilder("Phis in the Dark smoke test\n");
        List<String> errors = new ArrayList<>();
        Set<String> websiteIds = new HashSet<>();
        Set<String> websiteUrls = new HashSet<>();
        Set<String> puzzleIds = new HashSet<>();

        result.append("websites=").append(browser.getWebsites().size()).append('\n');
        result.append("savePath=").append(saveManager.getSaveFile(GameMode.NORMAL)).append('\n');

        validateImage("loading_screen.png", errors);
        validateImage("wallpaper_desktop.png", errors);
        validateImage("terminal_background.png", errors);
        validateImage("popup_warning.png", errors);
        validateImage("notification_asset.png", errors);
        validateImage("fake_error_screen.png", errors);
        validateImage("icon_browser.png", errors);
        validateImage("icon_terminal.png", errors);
        validateImage("icon_notes.png", errors);
        validateImage("icon_mail.png", errors);
        validateImage("icon_save.png", errors);
        validateImage("icon_load.png", errors);
        validateImage("icon_settings.png", errors);

        validateSound("typing.wav", errors);
        validateSound("notification.wav", errors);
        validateSound("glitch.wav", errors);
        validateSound("ambience_loop.wav", errors);
        validateSound("static_noise.wav", errors);
        validateSound("error.wav", errors);
        validateSound("button_click.wav", errors);
        validateSound("creepy_whisper.wav", errors);

        int puzzleCount = 0;
        for (Website website : browser.getWebsites()) {
            validateUnique("website id", website.getId(), websiteIds, errors);
            validateUnique("website url", website.getUrl(), websiteUrls, errors);
            validateImage(website.getScreenshotAsset(), errors);

            Puzzle puzzle = website.getPuzzle();
            if (puzzle != null) {
                puzzleCount++;
                validateUnique("puzzle id", puzzle.getId(), puzzleIds, errors);
                if (!puzzle.checkAnswer(puzzle.getTutorialAnswer())) {
                    errors.add("tutorial answer rejected for puzzle " + puzzle.getId());
                }
                if (puzzle.getRewardItem() == null || puzzle.getRewardItem().isBlank()) {
                    errors.add("missing reward item for puzzle " + puzzle.getId());
                }
            }

            result.append("site=").append(website.getId())
                    .append(", puzzle=")
                    .append(puzzle == null ? "exploration" : puzzle.getId())
                    .append('\n');
        }

        result.append("requiredPuzzles=").append(getRequiredPuzzleCount()).append('\n');
        if (puzzleCount != 3) {
            errors.add("expected 3 puzzle websites but found " + puzzleCount);
        }

        validatePuzzleAnswers(errors);
        validateProgressionRules(errors);

        if (errors.isEmpty()) {
            result.append("status=OK");
        } else {
            for (String error : errors) {
                result.append("error=").append(error).append('\n');
            }
            result.append("status=FAIL");
        }
        return result.toString();
    }

    private void validateUnique(String label, String value, Set<String> existingValues, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add("missing " + label);
            return;
        }
        if (!existingValues.add(value)) {
            errors.add("duplicate " + label + ": " + value);
        }
    }

    private void validateImage(String fileName, List<String> errors) {
        if (fileName == null || fileName.isBlank()) {
            errors.add("missing image file name");
            return;
        }
        if (AssetLoader.loadImage(fileName) == null) {
            errors.add("missing or unreadable image asset: " + fileName);
        }
    }

    private void validateSound(String fileName, List<String> errors) {
        if (fileName == null || fileName.isBlank()) {
            errors.add("missing sound file name");
            return;
        }
        if (!Files.isRegularFile(AssetLoader.soundPath(fileName))) {
            errors.add("missing sound asset: " + fileName);
        }
    }

    private void validatePuzzleAnswers(List<String> errors) {
        VariablePuzzle variablePuzzle = new VariablePuzzle();
        if (!variablePuzzle.checkAnswer("ghost")) {
            errors.add("variable puzzle rejected exact answer");
        }
        if (variablePuzzle.checkAnswer("password ghost")) {
            errors.add("variable puzzle accepted non-exact answer");
        }

        ConditionalPuzzle conditionalPuzzle = new ConditionalPuzzle();
        if (!conditionalPuzzle.checkAnswer("connect hidden_server")) {
            errors.add("conditional puzzle rejected exact command");
        }
        if (conditionalPuzzle.checkAnswer("hidden_server")) {
            errors.add("conditional puzzle accepted partial command");
        }

        LoopPuzzle loopPuzzle = new LoopPuzzle();
        if (!loopPuzzle.checkAnswer("8")) {
            errors.add("loop puzzle rejected exact answer");
        }
        if (loopPuzzle.checkAnswer("signal 8")) {
            errors.add("loop puzzle accepted non-exact answer");
        }
    }

    private void validateProgressionRules(List<String> errors) {
        player.clearProgress();
        validateUnlockedState("initial", "black_lantern_forum", errors);

        player.solvePuzzle("puzzle_variable_gate");
        player.getInventory().addItem("forum_key");
        validateUnlockedState("after forum_key", "moth_index", errors);

        player.solvePuzzle("puzzle_hidden_server");
        player.getInventory().addItem("server_route");
        validateUnlockedState("after server_route", "echo_market", errors);

        player.solvePuzzle("puzzle_signal_doubler");
        player.getInventory().addItem("trace_key");
        if (!canFinishGame()) {
            errors.add("finish command unavailable after all puzzle keys");
        }
    }

    private void validateUnlockedState(String state, String expectedWebsiteId, List<String> errors) {
        for (Website website : browser.getWebsites()) {
            Puzzle puzzle = website.getPuzzle();
            if (puzzle == null || player.hasSolved(puzzle.getId())) {
                continue;
            }
            boolean expectedUnlocked = expectedWebsiteId.equals(website.getId());
            if (isPuzzleUnlocked(website) != expectedUnlocked) {
                errors.add("unexpected unlock state during " + state + " for " + website.getId());
            }
        }
    }
}
