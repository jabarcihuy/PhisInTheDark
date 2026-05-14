package com.nulltrace.ui;

import com.nulltrace.assets.AssetLoader;
import com.nulltrace.core.Game;
import com.nulltrace.events.EventSystem;
import com.nulltrace.puzzle.Puzzle;
import com.nulltrace.website.Website;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DesktopUI extends JFrame {
    private final Game game;
    private final WallpaperPane desktopPane;
    private final JPanel taskbar;
    private final JLabel statusLabel;
    private final JLabel clockLabel;
    private final JLabel modeLabel;
    private final JLabel progressLabel;
    private final JLabel objectiveLabel;
    private final PopupManager popupManager;
    private final BufferedImage wallpaperImage;
    private final BufferedImage glitchOverlay;
    private final BufferedImage crtOverlay;
    private BrowserWindow browserWindow;
    private TerminalWindow terminalWindow;
    private TutorialGuideWindow tutorialGuideWindow;
    private EventSystem eventSystem;
    private Timer clockTimer;
    private Timer blinkTimer;
    private int wallpaperMode;
    private boolean blinkState;

    public DesktopUI(Game game) {
        super("Phis in the Dark");
        UiTheme.applyGlobalDefaults();
        this.game = game;
        this.desktopPane = new WallpaperPane();
        this.taskbar = new JPanel(new BorderLayout(8, 0));
        this.statusLabel = new JLabel(game.isTutorialMode() ? "Tutorial route ready" : "Trace monitor idle");
        this.clockLabel = new JLabel("00:00:00");
        this.modeLabel = UiTheme.pill(game.isTutorialMode() ? "TUTORIAL" : "NORMAL", game.isTutorialMode() ? UiTheme.BLUE : UiTheme.ACCENT);
        this.progressLabel = UiTheme.pill("KEYS 0/3", UiTheme.ACCENT);
        this.objectiveLabel = UiTheme.pill("NEXT: OPEN BROWSER", UiTheme.AMBER);
        this.popupManager = new PopupManager(this, desktopPane);
        this.wallpaperImage = AssetLoader.loadImage("wallpaper_desktop.png");
        this.glitchOverlay = AssetLoader.loadImage("glitch_overlay.png");
        this.crtOverlay = AssetLoader.loadImage("crt_overlay.png");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        buildLayout();
        buildDesktopIcons();
        pack();
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);
        startTaskbarAnimation();
    }

    private void buildLayout() {
        JPanel root = new JPanel(new BorderLayout());
        root.setPreferredSize(new Dimension(1180, 760));
        root.add(desktopPane, BorderLayout.CENTER);

        taskbar.setBackground(UiTheme.PANEL);
        taskbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.ACCENT_DIM),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        JPanel quickLaunch = new JPanel(new GridLayout(1, game.isTutorialMode() ? 5 : 7, 4, 0));
        quickLaunch.setOpaque(false);
        quickLaunch.add(taskButton("Browser", "icon_browser.png", event -> launchBrowser()));
        quickLaunch.add(taskButton("Terminal", "icon_terminal.png", event -> launchTerminal()));
        quickLaunch.add(taskButton("Notes", "icon_notes.png", event -> openNotes()));
        quickLaunch.add(taskButton("Mail", "icon_mail.png", event -> openEmail()));
        quickLaunch.add(taskButton("Settings", "icon_settings.png", event -> openSettings()));
        if (!game.isTutorialMode()) {
            quickLaunch.add(taskButton("Save", "icon_save.png", event -> {
                boolean saved = game.saveGame();
                showNotification(saved ? "SAVE OK" : "SAVE FAILED", saved ? game.progressSummary() : "Tidak bisa menulis save file.");
            }));
            quickLaunch.add(taskButton("Load", "icon_load.png", event -> {
                boolean loaded = game.loadGame();
                showNotification(loaded ? "LOAD OK" : "NO SAVE", loaded ? game.progressSummary() : "Save belum ada.");
            }));
        }

        JPanel statusCluster = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        statusCluster.setOpaque(false);
        statusCluster.add(modeLabel);
        statusCluster.add(progressLabel);
        statusCluster.add(objectiveLabel);

        statusLabel.setForeground(UiTheme.TEXT);
        statusLabel.setFont(UiTheme.font(12f, Font.BOLD));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        statusCluster.add(statusLabel);

        clockLabel.setForeground(UiTheme.TEXT_MUTED);
        clockLabel.setFont(UiTheme.font(12f, Font.PLAIN));
        clockLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        clockLabel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        taskbar.add(quickLaunch, BorderLayout.WEST);
        taskbar.add(statusCluster, BorderLayout.CENTER);
        taskbar.add(clockLabel, BorderLayout.EAST);
        root.add(taskbar, BorderLayout.SOUTH);
        setContentPane(root);
        updateProgressLabel();
    }

    private void buildDesktopIcons() {
        desktopPane.add(desktopIcon("Browser", "icon_browser.png", 30, 28, event -> launchBrowser()));
        desktopPane.add(desktopIcon("Terminal", "icon_terminal.png", 30, 122, event -> launchTerminal()));
        desktopPane.add(desktopIcon("Notes", "icon_notes.png", 30, 216, event -> openNotes()));
        desktopPane.add(desktopIcon("Mail", "icon_mail.png", 30, 310, event -> openEmail()));
        desktopPane.add(desktopIcon("Settings", "icon_settings.png", 130, 28, event -> openSettings()));
        if (!game.isTutorialMode()) {
            desktopPane.add(desktopIcon("Save", "icon_save.png", 30, 404, event -> {
                boolean saved = game.saveGame();
                showNotification(saved ? "SAVE OK" : "SAVE FAILED", saved ? game.progressSummary() : "Tidak bisa menulis save file.");
            }));
            desktopPane.add(desktopIcon("Load", "icon_load.png", 30, 498, event -> {
                boolean loaded = game.loadGame();
                showNotification(loaded ? "LOAD OK" : "NO SAVE", loaded ? game.progressSummary() : "Save belum ada.");
            }));
        }
    }

    private JButton desktopIcon(String label, String iconFile, int x, int y, java.awt.event.ActionListener action) {
        JButton button = new JButton("<html><center>" + label + "</center></html>");
        button.setBounds(x, y, 90, 78);
        javax.swing.ImageIcon icon = AssetLoader.loadIcon(iconFile, 40, 40);
        button.setIcon(icon != null ? icon : UIManager.getIcon("FileView.fileIcon"));
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        UiTheme.styleToolButton(button);
        button.setBackground(new Color(12, 22, 27));
        button.setFont(AssetLoader.terminalFont(11f, Font.BOLD));
        button.setToolTipText(label);
        button.addActionListener(action);
        applyButtonHover(button);
        return button;
    }

    private JButton taskButton(String label, String iconFile, java.awt.event.ActionListener action) {
        JButton button = new JButton(label);
        javax.swing.ImageIcon icon = AssetLoader.loadIcon(iconFile, 18, 18);
        if (icon != null) {
            button.setIcon(icon);
            button.setIconTextGap(6);
        }
        UiTheme.styleToolButton(button);
        button.setToolTipText(label);
        button.addActionListener(action);
        applyButtonHover(button);
        return button;
    }

    private void startTaskbarAnimation() {
        clockTimer = new Timer(500, event -> clockLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
        clockTimer.start();

        blinkTimer = new Timer(720, event -> {
            blinkState = !blinkState;
            Color color = blinkState ? new Color(9, 30, 34) : new Color(10, 14, 18);
            taskbar.setBackground(color);
        });
        blinkTimer.start();
    }

    public void launchBrowser() {
        if (browserWindow == null || browserWindow.isClosed()) {
            browserWindow = new BrowserWindow(game, this);
            desktopPane.add(browserWindow);
            browserWindow.setLocation(130, 42);
        }
        showInternal(browserWindow);
        updateTutorialGuide();
    }

    public void launchTerminal() {
        if (terminalWindow == null || terminalWindow.isClosed()) {
            terminalWindow = new TerminalWindow(game, this);
            desktopPane.add(terminalWindow);
            terminalWindow.setLocation(210, 110);
        }
        showInternal(terminalWindow);
        updateTutorialGuide();
    }

    public void openNotes() {
        String notes = "PHIS IN THE DARK NOTES\n\n"
                + "- Browser berisi 10 website misterius.\n"
                + "- Puzzle pendek, fokus pada logic dasar.\n"
                + "- Typo kecil tidak langsung menghukum. Baca hint jika gagal.\n"
                + "- Setelah 3 puzzle selesai, terminal menerima command: exit_trace\n\n"
                + "Progress: " + game.getPlayer().getSolvedPuzzleCount() + "/" + game.getRequiredPuzzleCount() + " key\n"
                + "Objective: " + currentObjectiveDetail() + "\n\n"
                + keyChecklist();
        if (game.isTutorialMode()) {
            notes += "\n\nTUTORIAL MODE\n"
                    + "- Ikuti panel Tutorial Guide.\n"
                    + "- Random popup, browser glitch, dan warning event dimatikan.\n"
                    + "- Tombol Isi Jawaban Tutorial tersedia di puzzle.\n"
                    + "- Tutorial selalu mulai dari awal dan tidak memakai save/load.";
        }
        openTextFile("notes.txt", notes);
    }

    public void openEmail() {
        openTextFile("mailbox.eml", mailboxText());
    }

    public void openSettings() {
        BaseWindow settings = new BaseWindow("Settings", 500, game.isTutorialMode() ? 260 : 330) {
        };
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UiTheme.PANEL);
        panel.setBorder(UiTheme.panelBorder(UiTheme.BLUE));

        JLabel title = new JLabel("SETTINGS", SwingConstants.CENTER);
        title.setForeground(UiTheme.BLUE);
        title.setFont(UiTheme.font(20f, Font.BOLD));

        JPanel actions = new JPanel(new GridLayout(game.isTutorialMode() ? 2 : 3, 1, 8, 8));
        actions.setOpaque(false);

        JButton mute = taskButton(game.getAudioManager().isMuted() ? "Unmute Audio" : "Mute Audio", "icon_terminal.png", event -> {
            boolean muted = !game.getAudioManager().isMuted();
            game.getAudioManager().setMuted(muted);
            showNotification("AUDIO", muted ? "Muted." : "Unmuted.");
            settings.dispose();
        });
        actions.add(mute);

        if (!game.isTutorialMode()) {
            JButton reset = taskButton("Reset Normal Save", "icon_load.png", event -> {
                int choice = JOptionPane.showConfirmDialog(this, "Hapus save normal lokal?", "Reset Save", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice == JOptionPane.OK_OPTION) {
                    boolean deleted = game.resetNormalSave();
                    showNotification(deleted ? "SAVE RESET" : "NO SAVE", deleted ? "Save normal dihapus." : "Tidak ada save untuk dihapus.");
                    settings.dispose();
                }
            });
            actions.add(reset);
        }

        JButton menu = taskButton("Menu Utama", "icon_browser.png", event -> game.returnToMainMenu());
        actions.add(menu);

        panel.add(title, BorderLayout.NORTH);
        panel.add(actions, BorderLayout.CENTER);
        settings.content.add(panel);
        desktopPane.add(settings, JDesktopPane.POPUP_LAYER);
        centerInternal(settings);
        showInternal(settings);
    }

    public void openTextFile(String title, String content) {
        TextWindow window = new TextWindow(title, content);
        desktopPane.add(window);
        centerInternal(window);
        showInternal(window);
    }

    public void openImageFile(String title, String imageFile) {
        ImageWindow window = new ImageWindow(title, imageFile, 560, 360);
        desktopPane.add(window);
        centerInternal(window);
        showInternal(window);
    }

    public void showNotification(String title, String message) {
        statusLabel.setText(title + ": " + message);
        updateProgressLabel();
        popupManager.showToast(title, message);
    }

    public void showPopup(String title, String message) {
        popupManager.showPopup(title, message);
    }

    public void onPuzzleSolved(Website website, Puzzle puzzle) {
        int solved = game.getPlayer().getSolvedPuzzleCount();
        wallpaperMode = Math.min(3, solved);
        desktopPane.repaint();
        showNotification("PUZZLE SOLVED", puzzle.getRewardItem() + " added.");
        openTextFile(website.getUnlockedFileTitle(), website.getUnlockedFileContent());

        if (game.canFinishGame()) {
            showNotification("EXIT READY", "Buka terminal dan ketik exit_trace.");
        }
        updateTutorialGuide();
        updateProgressLabel();
    }

    public void refreshAfterLoad() {
        wallpaperMode = Math.min(3, game.getPlayer().getSolvedPuzzleCount());
        desktopPane.repaint();
        if (browserWindow != null) {
            browserWindow.refreshCurrentPage();
        }
        if (terminalWindow != null) {
            terminalWindow.printStatus();
        }
        updateTutorialGuide();
        updateProgressLabel();
    }

    public void showCompletedRouteNotice() {
        BaseWindow notice = new BaseWindow("Route Archived", 520, 300) {
        };
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UiTheme.PANEL);
        panel.setBorder(UiTheme.panelBorder(UiTheme.BLUE));

        JLabel title = new JLabel("ROUTE ALREADY COMPLETE", SwingConstants.CENTER);
        title.setForeground(UiTheme.BLUE);
        title.setFont(UiTheme.font(20f, Font.BOLD));

        JLabel body = new JLabel("<html><body style='width:430px;text-align:center'>"
                + "Save ini sudah mencapai ending.<br><br>"
                + "Kamu bisa membaca desktop arsip, atau kembali ke menu utama untuk mulai route baru."
                + "</body></html>", SwingConstants.CENTER);
        body.setForeground(UiTheme.TEXT);
        body.setFont(UiTheme.font(13f, Font.PLAIN));

        JPanel actions = new JPanel(new GridLayout(1, 2, 8, 0));
        actions.setOpaque(false);
        JButton stay = taskButton("Buka Arsip", "icon_notes.png", event -> notice.dispose());
        JButton menu = taskButton("Menu Utama", "icon_terminal.png", event -> game.returnToMainMenu());
        actions.add(stay);
        actions.add(menu);

        panel.add(title, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        notice.content.add(panel);

        desktopPane.add(notice, JDesktopPane.POPUP_LAYER);
        centerInternal(notice);
        showInternal(notice);
    }

    public void triggerBrowserGlitch(String message) {
        if (browserWindow != null && browserWindow.isVisible()) {
            browserWindow.triggerGlitch(message);
            openImageFile("render_error.png", "fake_error_screen.png");
        } else {
            showPopup("BROWSER ERROR", message);
        }
    }

    public void corruptWallpaperTemporarily() {
        int oldMode = wallpaperMode;
        wallpaperMode = 9;
        desktopPane.repaint();
        Timer restore = new Timer(1300, event -> {
            wallpaperMode = oldMode;
            desktopPane.repaint();
        });
        restore.setRepeats(false);
        restore.start();
    }

    public void showEnding() {
        BaseWindow ending = new BaseWindow("Ending", 520, 340) {
        };
        ending.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
        ending.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent event) {
                game.returnToMainMenu();
            }
        });

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UiTheme.PANEL);
        panel.setBorder(UiTheme.panelBorder(UiTheme.ACCENT));

        JLabel title = new JLabel("TRACE COMPLETE", SwingConstants.CENTER);
        title.setForeground(UiTheme.ACCENT);
        title.setFont(UiTheme.font(24f, Font.BOLD));

        JLabel body = new JLabel("<html><body style='width:430px;text-align:center'>"
                + "Kamu menutup koneksi sebelum Phis in the Dark selesai menulis namamu.<br><br>"
                + keyChecklist().replace("\n", "<br>") + "<br><br>"
                + "Trace selesai. Kamu akan kembali ke menu utama."
                + "</body></html>", SwingConstants.CENTER);
        body.setForeground(BaseWindow.TEXT_MAIN);
        body.setFont(UiTheme.font(13f, Font.PLAIN));

        JButton close = taskButton("Menu Utama", "icon_terminal.png", event -> game.returnToMainMenu());
        panel.add(title, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);
        panel.add(close, BorderLayout.SOUTH);
        ending.content.add(panel);

        desktopPane.add(ending, JDesktopPane.POPUP_LAYER);
        centerInternal(ending);
        showInternal(ending);

        Timer returnTimer = new Timer(4200, event -> game.returnToMainMenu());
        returnTimer.setRepeats(false);
        returnTimer.start();
    }

    @Override
    public void dispose() {
        if (clockTimer != null) {
            clockTimer.stop();
        }
        if (blinkTimer != null) {
            blinkTimer.stop();
        }
        super.dispose();
    }

    public void showTutorialGuide() {
        if (!game.isTutorialMode()) {
            return;
        }

        if (tutorialGuideWindow == null || tutorialGuideWindow.isClosed()) {
            tutorialGuideWindow = new TutorialGuideWindow(game);
            desktopPane.add(tutorialGuideWindow, JDesktopPane.POPUP_LAYER);
            tutorialGuideWindow.setLocation(700, 60);
        }
        showInternal(tutorialGuideWindow);
    }

    public void updateTutorialGuide() {
        if (tutorialGuideWindow != null && !tutorialGuideWindow.isClosed()) {
            tutorialGuideWindow.update();
        }
    }

    public void setEventSystem(EventSystem eventSystem) {
        this.eventSystem = eventSystem;
    }

    public EventSystem getEventSystem() {
        return eventSystem;
    }

    public JDesktopPane getDesktopPane() {
        return desktopPane;
    }

    public void applyButtonHover(AbstractButton button) {
        Color normal = button.getBackground();
        Color hover = new Color(
                Math.min(255, normal.getRed() + 18),
                Math.min(255, normal.getGreen() + 34),
                Math.min(255, normal.getBlue() + 36)
        );
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                button.setBackground(hover);
            }

            @Override
            public void mousePressed(MouseEvent event) {
                game.getAudioManager().playButtonClick();
            }

            @Override
            public void mouseExited(MouseEvent event) {
                button.setBackground(normal);
            }
        });
    }

    private void showInternal(JInternalFrame frame) {
        if (frame instanceof BaseWindow baseWindow) {
            baseWindow.prepareForDisplay();
        } else {
            frame.revalidate();
            frame.repaint();
        }
        frame.setVisible(true);
        frame.toFront();
        try {
            frame.setSelected(true);
        } catch (PropertyVetoException ignored) {
            frame.requestFocus();
        }
    }

    private void centerInternal(Component component) {
        int x = Math.max(20, (desktopPane.getWidth() - component.getWidth()) / 2);
        int y = Math.max(20, (desktopPane.getHeight() - component.getHeight()) / 2);
        component.setLocation(x, y);
    }

    private void updateProgressLabel() {
        int solved = game.getPlayer().getSolvedPuzzleCount();
        progressLabel.setText("KEYS " + solved + "/" + game.getRequiredPuzzleCount());
        progressLabel.setForeground(solved >= game.getRequiredPuzzleCount() ? Color.BLACK : UiTheme.TEXT);
        progressLabel.setBackground(solved >= game.getRequiredPuzzleCount() ? UiTheme.ACCENT : new Color(7, 31, 27));
        objectiveLabel.setText("NEXT: " + currentObjectiveShort());
        objectiveLabel.setBackground(game.canFinishGame() ? new Color(42, 34, 9) : new Color(32, 23, 7));
    }

    private String currentObjectiveShort() {
        if (game.getPlayer().isEndingReached()) {
            return "ROUTE ARCHIVED";
        }
        if (game.canFinishGame()) {
            return "EXIT_TRACE";
        }
        if (!game.getPlayer().getInventory().hasItem("forum_key")) {
            return "BLACK LANTERN";
        }
        if (!game.getPlayer().getInventory().hasItem("server_route")) {
            return "MOTH INDEX";
        }
        return "ECHO MARKET";
    }

    private String currentObjectiveDetail() {
        if (game.getPlayer().isEndingReached()) {
            return "Route selesai. Load desktop ini hanya untuk membaca arsip.";
        }
        if (game.canFinishGame()) {
            return "Buka Terminal, lalu ketik exit_trace.";
        }
        if (!game.getPlayer().getInventory().hasItem("forum_key")) {
            return "Buka Browser, pilih Black Lantern Forum, lalu selesaikan Variable Gate.";
        }
        if (!game.getPlayer().getInventory().hasItem("server_route")) {
            return "Buka Browser, pilih Moth Index, lalu jalankan command yang aktif di if branch.";
        }
        return "Buka Browser, pilih Echo Market, lalu hitung nilai akhir Signal Doubler.";
    }

    private String keyChecklist() {
        return "KEY CHECKLIST\n"
                + checkbox("forum_key") + " Forum Key - Black Lantern Forum\n"
                + checkbox("server_route") + " Server Route - Moth Index\n"
                + checkbox("trace_key") + " Trace Key - Echo Market";
    }

    private String checkbox(String item) {
        return game.getPlayer().getInventory().hasItem(item) ? "[x]" : "[ ]";
    }

    private String mailboxText() {
        if (game.getPlayer().isEndingReached()) {
            return "INBOX // ARCHIVED\n\n"
                    + "FROM: system.phis-dark\nSUBJECT: route closed\n\n"
                    + "Trace sudah ditutup. Save ini tersimpan sebagai arsip selesai.\n\n"
                    + "FROM: unknown_user\nSUBJECT: afterimage\n\n"
                    + "Kamu kembali ke menu utama, tapi cache halaman tetap tahu urutan puzzle.";
        }
        if (game.getPlayer().getInventory().hasItem("trace_key")) {
            return "INBOX // 1 UNREAD\n\n"
                    + "FROM: unknown_user\nSUBJECT: exit command\n\n"
                    + "Tiga key sudah terkumpul.\n"
                    + "Buka Terminal dan ketik: exit_trace";
        }
        if (game.getPlayer().getInventory().hasItem("server_route")) {
            return "INBOX // 1 UNREAD\n\n"
                    + "FROM: unknown_user\nSUBJECT: route\n\n"
                    + "Kamu sudah menemukan hidden_server.\n"
                    + "Sekarang cari halaman yang menggandakan signal sampai angka akhirnya jelas.";
        }
        if (game.getPlayer().getInventory().hasItem("forum_key")) {
            return "INBOX // 1 UNREAD\n\n"
                    + "FROM: root_mirror\nSUBJECT: index dingin\n\n"
                    + "Forum key diterima.\n"
                    + "Cari Moth Index. If branch yang aktif akan memberi route berikutnya.";
        }
        return "INBOX\n\nTidak ada email baru.\n\nSatu pesan terlihat terkunci di folder draft.";
    }

    private class WallpaperPane extends JDesktopPane {
        WallpaperPane() {
            setBackground(new Color(4, 7, 10));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (wallpaperImage != null) {
                g2.drawImage(wallpaperImage, 0, 0, getWidth(), getHeight(), null);
            } else {
                Color top = wallpaperMode >= 2 ? new Color(16, 18, 30) : new Color(6, 12, 16);
                Color bottom = wallpaperMode >= 1 ? new Color(22, 44, 40) : new Color(12, 19, 24);
                g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            g2.setColor(new Color(49, 220, 176, 35));
            for (int x = 0; x < getWidth(); x += 46) {
                g2.drawLine(x, 0, x, getHeight());
            }
            for (int y = 0; y < getHeight(); y += 38) {
                g2.drawLine(0, y, getWidth(), y);
            }

            g2.setFont(UiTheme.font(42f, Font.BOLD));
            g2.setColor(new Color(49, 220, 176, 70));
            g2.drawString("PHIS IN THE DARK", Math.max(160, getWidth() - 560), Math.max(120, getHeight() / 3));

            g2.setFont(UiTheme.font(13f, Font.PLAIN));
            g2.setColor(new Color(220, 245, 240, 120));
            g2.drawString("fake desktop / education build / paranoia low", Math.max(160, getWidth() - 430), Math.max(150, getHeight() / 3 + 30));

            String progress = "keys " + game.getPlayer().getSolvedPuzzleCount() + "/" + game.getRequiredPuzzleCount();
            int panelWidth = 178;
            int panelHeight = 46;
            int panelX = Math.max(130, getWidth() - panelWidth - 28);
            int panelY = 24;
            g2.setColor(new Color(4, 9, 12, 160));
            g2.fillRect(panelX, panelY, panelWidth, panelHeight);
            g2.setColor(new Color(49, 220, 176, 120));
            g2.drawRect(panelX, panelY, panelWidth, panelHeight);
            g2.setFont(UiTheme.font(12f, Font.BOLD));
            g2.setColor(new Color(226, 244, 240, 190));
            g2.drawString(game.isTutorialMode() ? "tutorial route" : "normal route", panelX + 12, panelY + 18);
            g2.setColor(new Color(49, 220, 176, 210));
            g2.drawString(progress, panelX + 12, panelY + 34);

            g2.setStroke(new BasicStroke(1f));
            for (int y = 0; y < getHeight(); y += 4) {
                g2.setColor(new Color(0, 0, 0, 35));
                g2.drawLine(0, y, getWidth(), y);
            }

            if (wallpaperMode == 9 || wallpaperMode >= 3) {
                if (glitchOverlay != null) {
                    g2.drawImage(glitchOverlay, 0, 0, getWidth(), getHeight(), null);
                }
                g2.setColor(new Color(255, 79, 112, wallpaperMode == 9 ? 90 : 35));
                for (int y = 30; y < getHeight(); y += 90) {
                    g2.fillRect(0, y, getWidth(), 9);
                }
                g2.setColor(new Color(49, 220, 176, 70));
                for (int x = 120; x < getWidth(); x += 180) {
                    g2.drawString("WHO_TYPED_EXIT", x, 70 + (x % 180));
                }
            }

            if (crtOverlay != null) {
                g2.drawImage(crtOverlay, 0, 0, getWidth(), getHeight(), null);
            }

            g2.dispose();
        }
    }
}
