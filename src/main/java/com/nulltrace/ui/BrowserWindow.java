package com.nulltrace.ui;

import com.nulltrace.assets.AssetLoader;
import com.nulltrace.core.Game;
import com.nulltrace.puzzle.Puzzle;
import com.nulltrace.website.Website;

import javax.swing.BorderFactory;
import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class BrowserWindow extends BaseWindow {
    private static final int BROWSER_WIDTH = 920;
    private static final int BROWSER_HEIGHT = 640;
    private static final int PREVIEW_WIDTH = 330;
    private static final int PREVIEW_HEIGHT = 206;

    private final Game game;
    private final DesktopUI desktopUI;
    private final JComboBox<Website> siteSelector;
    private final JTextField addressField;
    private final JLabel pageStatusLabel;
    private final JLabel browserProgressLabel;
    private final JPanel pagePanel;
    private final Map<String, Integer> attempts;
    private Website currentWebsite;

    public BrowserWindow(Game game, DesktopUI desktopUI) {
        super("Browser", BROWSER_WIDTH, BROWSER_HEIGHT);
        this.game = game;
        this.desktopUI = desktopUI;
        this.siteSelector = new JComboBox<>();
        this.addressField = new JTextField();
        this.pageStatusLabel = UiTheme.pill("IDLE", UiTheme.ACCENT);
        this.browserProgressLabel = UiTheme.pill("PROGRESS 0/3", UiTheme.BLUE);
        this.pagePanel = new JPanel(new BorderLayout(10, 10));
        this.pagePanel.setPreferredSize(new Dimension(BROWSER_WIDTH - 54, BROWSER_HEIGHT - 142));
        this.attempts = new HashMap<>();

        buildToolbar();
        JScrollPane pageScroll = new JScrollPane(pagePanel);
        pageScroll.setBorder(BorderFactory.createLineBorder(new Color(28, 43, 47), 1));
        pageScroll.getViewport().setBackground(UiTheme.BACKDROP);
        pageScroll.setPreferredSize(new Dimension(BROWSER_WIDTH - 32, BROWSER_HEIGHT - 106));
        content.add(pageScroll, BorderLayout.CENTER);

        for (Website website : game.getBrowser().getWebsites()) {
            if (!game.isTutorialMode() || website.getPuzzle() != null) {
                siteSelector.addItem(website);
            }
        }
        siteSelector.addActionListener(event -> renderSelectedSite());
        if (siteSelector.getItemCount() > 0) {
            siteSelector.setSelectedIndex(0);
            renderSelectedSite();
        }
    }

    private void buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(8, 8));
        toolbar.setBackground(UiTheme.PANEL);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        UiTheme.styleAddressField(addressField);

        JButton reload = new JButton("Reload");
        UiTheme.styleToolButton(reload);
        reload.addActionListener(event -> {
            desktopUI.showNotification("BROWSER", "Page refreshed.");
            renderSelectedSite();
        });
        desktopUI.applyButtonHover(reload);

        UiTheme.styleComboBox(siteSelector);
        siteSelector.setPreferredSize(new Dimension(220, 32));
        siteSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Website website) {
                    label.setText(siteLabel(website));
                }
                return label;
            }
        });

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.add(siteSelector, BorderLayout.WEST);
        row.add(addressField, BorderLayout.CENTER);
        row.add(reload, BorderLayout.EAST);

        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        statusRow.setOpaque(false);
        statusRow.add(pageStatusLabel);
        statusRow.add(browserProgressLabel);

        toolbar.add(row, BorderLayout.NORTH);
        toolbar.add(statusRow, BorderLayout.SOUTH);
        content.add(toolbar, BorderLayout.NORTH);
    }

    private void renderSelectedSite() {
        Website selected = (Website) siteSelector.getSelectedItem();
        if (selected != null) {
            renderWebsite(selected);
        }
    }

    private void renderWebsite(Website website) {
        currentWebsite = website;
        game.getBrowser().visit(website);
        addressField.setText(website.getUrl());
        updatePageStatus(website);
        updateProgressLabel();

        pagePanel.removeAll();
        pagePanel.setBackground(backgroundFor(website));
        pagePanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        pagePanel.add(buildHeader(website), BorderLayout.NORTH);
        pagePanel.add(buildBody(website), BorderLayout.CENTER);
        pagePanel.add(buildPuzzlePanel(website), BorderLayout.SOUTH);
        pagePanel.revalidate();
        pagePanel.doLayout();
        pagePanel.repaint();
        desktopUI.updateTutorialGuide();
    }

    private JPanel buildHeader(Website website) {
        JPanel header = new JPanel(new BorderLayout(4, 4));
        header.setOpaque(false);

        JLabel title = new JLabel(website.getTitle());
        title.setForeground(accentFor(website));
        title.setFont(AssetLoader.terminalFont(24f, Font.BOLD));

        JLabel subtitle = new JLabel(website.getSubtitle());
        subtitle.setForeground(new Color(188, 206, 202));
        subtitle.setFont(AssetLoader.terminalFont(12f, Font.PLAIN));

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        return header;
    }

    private Component buildBody(Website website) {
        JPanel bodyPanel = new JPanel(new BorderLayout(8, 8));
        bodyPanel.setOpaque(false);

        BufferedImage screenshot = AssetLoader.loadImage(website.getScreenshotAsset());
        if (screenshot != null) {
            Image scaled = screenshot.getScaledInstance(PREVIEW_WIDTH, PREVIEW_HEIGHT, Image.SCALE_SMOOTH);
            JLabel preview = new JLabel(new ImageIcon(scaled));
            preview.setBorder(BorderFactory.createLineBorder(accentFor(website), 1));
            JPanel holder = new JPanel(new BorderLayout());
            holder.setOpaque(false);
            holder.setPreferredSize(new Dimension(PREVIEW_WIDTH + 2, PREVIEW_HEIGHT + 2));
            holder.add(preview, BorderLayout.NORTH);
            bodyPanel.add(holder, BorderLayout.WEST);
        }

        JTextArea body = new JTextArea(String.join("\n\n", website.getBodyLines()));
        body.setEditable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setFont(AssetLoader.terminalFont(14f, Font.PLAIN));
        body.setBackground(new Color(6, 10, 12));
        body.setForeground(new Color(225, 235, 232));
        body.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JScrollPane scroll = UiTheme.wrapScroll(body);
        scroll.setPreferredSize(new Dimension(430, PREVIEW_HEIGHT + 2));
        bodyPanel.add(scroll, BorderLayout.CENTER);
        return bodyPanel;
    }

    private JPanel buildPuzzlePanel(Website website) {
        Puzzle puzzle = website.getPuzzle();
        if (puzzle == null) {
            return buildExplorationPanel(website);
        }
        boolean solved = game.getPlayer().hasSolved(puzzle.getId());
        if (!solved && !game.isPuzzleUnlocked(website)) {
            return buildLockedPuzzlePanel(website, puzzle);
        }

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UiTheme.PANEL);
        panel.setBorder(UiTheme.panelBorder(accentFor(website)));

        JLabel title = new JLabel(puzzle.getTitle() + "  [" + puzzle.getConcept() + "]");
        title.setForeground(accentFor(website));
        title.setFont(AssetLoader.terminalFont(14f, Font.BOLD));
        JPanel heading = new JPanel(new BorderLayout(8, 0));
        heading.setOpaque(false);
        heading.add(title, BorderLayout.WEST);
        heading.add(UiTheme.pill(solved ? "SOLVED" : "ACTIVE", solved ? UiTheme.ACCENT : UiTheme.AMBER), BorderLayout.EAST);
        panel.add(heading, BorderLayout.NORTH);

        if (solved) {
            panel.add(buildSolvedPanel(website, puzzle), BorderLayout.CENTER);
            return panel;
        }

        JPanel center = new JPanel(new GridLayout(1, 2, 10, 0));
        center.setOpaque(false);
        center.setPreferredSize(new Dimension(BROWSER_WIDTH - 82, 190));

        JTextArea code = new JTextArea(puzzle.getCodeBlock());
        code.setEditable(false);
        code.setFont(AssetLoader.terminalFont(13f, Font.PLAIN));
        code.setBackground(new Color(5, 8, 11));
        code.setForeground(new Color(212, 238, 232));
        code.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        code.setRows(8);
        center.add(UiTheme.wrapScroll(code));

        JPanel answerPanel = new JPanel(new BorderLayout(6, 6));
        answerPanel.setOpaque(false);
        JLabel instruction = new JLabel("<html><body style='width:260px'>" + escape(puzzle.getInstruction()) + "</body></html>");
        instruction.setForeground(new Color(218, 230, 230));
        instruction.setFont(AssetLoader.terminalFont(12f, Font.PLAIN));

        JTextArea answer = new JTextArea(3, 22);
        UiTheme.styleTextComponent(answer, accentFor(website));

        JTextArea hint = new JTextArea(game.isTutorialMode()
                ? "Tutorial: klik Isi Jawaban Tutorial, lalu Submit."
                : attemptHint(puzzle, 0));
        hint.setEditable(false);
        hint.setLineWrap(true);
        hint.setWrapStyleWord(true);
        hint.setOpaque(false);
        hint.setForeground(new Color(186, 205, 198));
        hint.setFont(AssetLoader.terminalFont(11f, Font.PLAIN));

        JButton submit = new JButton("Submit");
        UiTheme.styleButton(submit, accentFor(website));
        submit.addActionListener(event -> submitAnswer(website, answer, hint, submit));
        desktopUI.applyButtonHover(submit);

        JButton fillTutorial = new JButton("Isi Jawaban Tutorial");
        UiTheme.styleButton(fillTutorial, UiTheme.BLUE);
        fillTutorial.addActionListener(event -> {
            answer.setText(puzzle.getTutorialAnswer());
            hint.setText("Jawaban tutorial sudah diisi. Tekan Submit.");
        });
        desktopUI.applyButtonHover(fillTutorial);

        answer.getInputMap().put(KeyStroke.getKeyStroke("ctrl ENTER"), "submitPuzzle");
        answer.getActionMap().put("submitPuzzle", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                submit.doClick();
            }
        });

        answerPanel.add(instruction, BorderLayout.NORTH);
        answerPanel.add(UiTheme.wrapScroll(answer), BorderLayout.CENTER);
        answerPanel.add(hint, BorderLayout.SOUTH);

        JPanel east = new JPanel(new BorderLayout(6, 6));
        east.setOpaque(false);
        east.add(answerPanel, BorderLayout.CENTER);
        JPanel actions = new JPanel(new GridLayout(1, game.isTutorialMode() ? 2 : 1, 6, 0));
        actions.setOpaque(false);
        if (game.isTutorialMode()) {
            actions.add(fillTutorial);
        }
        actions.add(submit);

        east.add(actions, BorderLayout.SOUTH);
        center.add(east);

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildLockedPuzzlePanel(Website website, Puzzle puzzle) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UiTheme.PANEL);
        panel.setBorder(UiTheme.panelBorder(accentFor(website)));

        JLabel title = new JLabel(puzzle.getTitle() + "  [" + puzzle.getConcept() + "]");
        title.setForeground(accentFor(website));
        title.setFont(AssetLoader.terminalFont(14f, Font.BOLD));

        JLabel message = new JLabel("<html><body style='width:560px'>"
                + "Puzzle ini belum terbuka. Ikuti objective berikutnya dulu: "
                + escape(game.progressSummary())
                + "</body></html>");
        message.setForeground(new Color(218, 230, 230));
        message.setFont(AssetLoader.terminalFont(12f, Font.PLAIN));

        panel.add(title, BorderLayout.NORTH);
        panel.add(message, BorderLayout.CENTER);
        panel.add(UiTheme.pill("LOCKED", UiTheme.WARNING), BorderLayout.EAST);
        return panel;
    }

    private JPanel buildExplorationPanel(Website website) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UiTheme.PANEL);
        panel.setBorder(UiTheme.panelBorder(accentFor(website)));

        JLabel title = new JLabel("Exploration Page");
        title.setForeground(accentFor(website));
        title.setFont(AssetLoader.terminalFont(14f, Font.BOLD));

        JLabel message = new JLabel("<html><body style='width:520px'>Tidak semua halaman berisi puzzle. Halaman ini memberi atmosfer, clue, atau file kecil untuk dibaca.</body></html>");
        message.setForeground(new Color(218, 230, 230));
        message.setFont(AssetLoader.terminalFont(12f, Font.PLAIN));

        JButton openFile = new JButton("Open " + website.getUnlockedFileTitle());
        UiTheme.styleButton(openFile, accentFor(website));
        openFile.addActionListener(event -> desktopUI.openTextFile(website.getUnlockedFileTitle(), website.getUnlockedFileContent()));
        desktopUI.applyButtonHover(openFile);

        panel.add(title, BorderLayout.NORTH);
        panel.add(message, BorderLayout.CENTER);
        panel.add(openFile, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildSolvedPanel(Website website, Puzzle puzzle) {
        JPanel solved = new JPanel(new BorderLayout(8, 8));
        solved.setOpaque(false);

        JLabel message = new JLabel("<html><body style='width:580px'>" + escape(puzzle.getSolvedMessage()) + "</body></html>");
        message.setForeground(new Color(218, 245, 235));
        message.setFont(AssetLoader.terminalFont(13f, Font.PLAIN));

        JButton openFile = new JButton("Open " + website.getUnlockedFileTitle());
        UiTheme.styleButton(openFile, accentFor(website));
        openFile.addActionListener(event -> desktopUI.openTextFile(website.getUnlockedFileTitle(), website.getUnlockedFileContent()));
        desktopUI.applyButtonHover(openFile);

        solved.add(message, BorderLayout.CENTER);
        solved.add(openFile, BorderLayout.EAST);
        return solved;
    }

    private void submitAnswer(Website website, JTextArea answer, JTextArea hint, JButton submit) {
        Puzzle puzzle = website.getPuzzle();
        if (!game.getPlayer().hasSolved(puzzle.getId()) && !game.isPuzzleUnlocked(website)) {
            hint.setText("Puzzle ini masih locked. Ikuti objective berikutnya dulu.");
            desktopUI.showNotification("LOCKED", game.progressSummary());
            game.getAudioManager().playError();
            return;
        }
        if (puzzle.checkAnswer(answer.getText())) {
            submit.setEnabled(false);
            showUnlockAnimation(website);
            return;
        }

        int nextAttempt = attempts.getOrDefault(puzzle.getId(), 0) + 1;
        attempts.put(puzzle.getId(), nextAttempt);
        if (game.isTutorialMode()) {
            answer.setText(puzzle.getTutorialAnswer());
            hint.setText("Tutorial mengisi jawaban yang benar. Tekan Submit untuk lanjut.");
            answer.setBorder(UiTheme.fieldBorder(UiTheme.BLUE));
            desktopUI.showNotification("TUTORIAL", "Jawaban sudah dibantu otomatis.");
            return;
        }
        hint.setText(attemptHint(puzzle, nextAttempt));
        answer.setBorder(UiTheme.fieldBorder(UiTheme.WARNING));
        answer.requestFocusInWindow();
        answer.selectAll();
        desktopUI.showNotification("TRY AGAIN", "Jawaban hampir selalu ada di potongan kode.");
        game.getAudioManager().playError();
    }

    private void showUnlockAnimation(Website website) {
        pagePanel.removeAll();
        pagePanel.setBackground(backgroundFor(website));
        pagePanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel label = new JLabel("decrypting page fragment...", SwingConstants.CENTER);
        label.setForeground(accentFor(website));
        label.setFont(AssetLoader.terminalFont(18f, Font.BOLD));

        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        UiTheme.styleProgress(bar, accentFor(website));

        JPanel loading = new JPanel(new BorderLayout(12, 12));
        loading.setOpaque(false);
        loading.add(label, BorderLayout.CENTER);
        loading.add(bar, BorderLayout.SOUTH);
        pagePanel.add(loading, BorderLayout.CENTER);
        pagePanel.revalidate();
        pagePanel.repaint();

        Timer timer = new Timer(900, event -> {
            game.puzzleSolved(website, website.getPuzzle());
            renderWebsite(website);
        });
        timer.setRepeats(false);
        timer.start();
    }

    public void refreshCurrentPage() {
        if (currentWebsite != null) {
            renderWebsite(currentWebsite);
        }
    }

    public void triggerGlitch(String message) {
        setTitle("Browser // GLITCH");
        content.setBorder(BorderFactory.createLineBorder(new Color(255, 79, 112), 3));
        desktopUI.showNotification("BROWSER GLITCH", message);

        Timer timer = new Timer(700, event -> {
            setTitle("Browser");
            resetContentBorder();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private Color backgroundFor(Website website) {
        return switch (website.getVisualStyle()) {
            case FORUM -> new Color(11, 22, 22);
            case WIKI -> new Color(22, 20, 16);
            case ENCRYPTED -> new Color(18, 12, 23);
            case MARKET -> new Color(25, 10, 18);
            case CHAT -> new Color(8, 16, 24);
            case CORRUPTED -> new Color(24, 7, 12);
            case BLOG -> new Color(9, 18, 12);
            case ARCHIVE -> new Color(18, 22, 25);
            case CONSPIRACY -> new Color(28, 17, 8);
            case SEARCH -> new Color(4, 13, 15);
        };
    }

    private Color accentFor(Website website) {
        return switch (website.getVisualStyle()) {
            case FORUM -> new Color(49, 220, 176);
            case WIKI -> new Color(226, 194, 111);
            case ENCRYPTED -> new Color(140, 170, 255);
            case MARKET -> new Color(255, 87, 112);
            case CHAT -> new Color(104, 220, 255);
            case CORRUPTED -> new Color(255, 87, 112);
            case BLOG -> new Color(190, 255, 170);
            case ARCHIVE -> new Color(210, 226, 240);
            case CONSPIRACY -> new Color(255, 164, 92);
            case SEARCH -> new Color(49, 220, 176);
        };
    }

    private void updatePageStatus(Website website) {
        Puzzle puzzle = website.getPuzzle();
        if (puzzle == null) {
            pageStatusLabel.setText("EXPLORATION");
            return;
        }
        if (game.getPlayer().hasSolved(puzzle.getId())) {
            pageStatusLabel.setText("SOLVED / " + puzzle.getRewardItem());
        } else {
            pageStatusLabel.setText("PUZZLE / " + puzzle.getConcept());
        }
    }

    private void updateProgressLabel() {
        browserProgressLabel.setText("PROGRESS " + game.getPlayer().getSolvedPuzzleCount() + "/" + game.getRequiredPuzzleCount());
    }

    private String siteLabel(Website website) {
        Puzzle puzzle = website.getPuzzle();
        if (puzzle == null) {
            return "[INFO] " + website.getTitle();
        }
        if (game.getPlayer().hasSolved(puzzle.getId())) {
            return "[SOLVED] " + website.getTitle();
        }
        if (isNextWebsite(website)) {
            return "[NEXT] " + website.getTitle();
        }
        return "[LOCKED] " + website.getTitle();
    }

    private boolean isNextWebsite(Website website) {
        return game.isPuzzleUnlocked(website);
    }

    private String attemptHint(Puzzle puzzle, int attempt) {
        return "Attempt " + Math.min(attempt + 1, 4) + " - " + puzzle.getHint(attempt);
    }

    private String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
