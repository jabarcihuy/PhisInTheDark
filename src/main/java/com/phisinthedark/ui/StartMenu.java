package com.phisinthedark.ui;

import com.phisinthedark.assets.AssetLoader;
import com.phisinthedark.core.GameMode;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class StartMenu extends JFrame {
    public record StartChoice(GameMode mode, boolean loadSave) {
    }

    private final Consumer<StartChoice> onStartSelected;
    private final BufferedImage background;
    private final String saveStatus;

    public StartMenu(Consumer<StartChoice> onStartSelected, String saveStatus) {
        super("Phis in the Dark");
        this.onStartSelected = onStartSelected;
        this.saveStatus = saveStatus;
        this.background = AssetLoader.loadImage("wallpaper_desktop.png");
        buildMenu();
    }

    private void buildMenu() {
        UiTheme.applyGlobalDefaults();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(820, 560));

        JPanel root = new JPanel(new BorderLayout(24, 24)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (background != null) {
                    g2.drawImage(background, 0, 0, getWidth(), getHeight(), null);
                }
                g2.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 190), 0, getHeight(), new Color(0, 0, 0, 130)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(49, 220, 176, 28));
                for (int x = 0; x < getWidth(); x += 42) {
                    g2.drawLine(x, 0, x, getHeight());
                }
                for (int y = 0; y < getHeight(); y += 36) {
                    g2.drawLine(0, y, getWidth(), y);
                }
                g2.dispose();
            }
        };
        root.setPreferredSize(new Dimension(960, 620));
        root.setBorder(BorderFactory.createEmptyBorder(42, 58, 36, 58));

        JLabel title = new JLabel("PHIS IN THE DARK", SwingConstants.CENTER);
        title.setForeground(UiTheme.ACCENT);
        title.setFont(AssetLoader.terminalFont(54f, Font.BOLD));

        JLabel subtitle = new JLabel("psychological horror hacking simulator // beginner logic route", SwingConstants.CENTER);
        subtitle.setForeground(UiTheme.TEXT);
        subtitle.setFont(AssetLoader.terminalFont(14f, Font.PLAIN));

        JPanel header = new JPanel(new BorderLayout(6, 6));
        header.setOpaque(false);
        header.add(title, BorderLayout.CENTER);
        header.add(subtitle, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(24, 0));
        center.setOpaque(false);
        center.add(buildRoutePanel(), BorderLayout.CENTER);
        center.add(buildActionPanel(), BorderLayout.EAST);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        footer.setOpaque(false);
        footer.add(UiTheme.pill("3 PUZZLES", UiTheme.ACCENT));
        footer.add(UiTheme.pill("FAKE DESKTOP", UiTheme.BLUE));
        footer.add(UiTheme.pill("BEGINNER LOGIC", UiTheme.AMBER));

        root.add(header, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildRoutePanel() {
        JPanel panel = glassPanel(new BorderLayout(12, 12), UiTheme.ACCENT_DIM);
        panel.setPreferredSize(new Dimension(410, 320));

        JLabel heading = new JLabel("ROUTE STATUS");
        heading.setForeground(UiTheme.ACCENT);
        heading.setFont(AssetLoader.terminalFont(18f, Font.BOLD));

        JLabel body = new JLabel("<html><body style='width:330px'>"
                + "<b>Normal</b><br>"
                + "Random popup, warning, ambience, dan glitch event aktif.<br><br>"
                + "<b>Tutorial</b><br>"
                + "Guide panel aktif, puzzle bisa dibantu otomatis, event horror random dimatikan.<br><br>"
                + "<b>Objective</b><br>"
                + "Selesaikan 3 puzzle logic, lalu ketik <span style='color:#31DCB0'>exit_trace</span> di terminal.<br><br>"
                + "<b>Save</b><br>"
                + escape(saveStatus)
                + "</body></html>");
        body.setForeground(UiTheme.TEXT);
        body.setFont(AssetLoader.terminalFont(13f, Font.PLAIN));
        body.setVerticalAlignment(SwingConstants.TOP);

        JPanel pills = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pills.setOpaque(false);
        pills.add(UiTheme.pill("VARIABLE", UiTheme.ACCENT));
        pills.add(UiTheme.pill("IF ELSE", UiTheme.AMBER));
        pills.add(UiTheme.pill("LOOP", UiTheme.BLUE));

        panel.add(heading, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);
        panel.add(pills, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildActionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(360, 280));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 10, 0);

        constraints.gridy = 0;
        panel.add(menuButton("NORMAL / NEW", "Game Baru", "Mulai dari desktop kosong, random event aktif.", GameMode.NORMAL, false), constraints);
        constraints.gridy = 1;
        panel.add(menuButton("NORMAL / LOAD", "Load Game", loadGameDetail(), GameMode.NORMAL, true), constraints);
        constraints.gridy = 2;
        constraints.insets = new Insets(0, 0, 0, 0);
        panel.add(menuButton("GUIDED ROUTE", "Tutorial", "Mulai tutorial dari awal tanpa save/load.", GameMode.TUTORIAL, false), constraints);
        return panel;
    }

    private JButton menuButton(String eyebrow, String title, String detail, GameMode mode, boolean loadSave) {
        Color accent = routeAccent(mode, loadSave);
        JButton button = new JButton("<html><body style='width:285px;text-align:left'>"
                + "<span style='font-size:10px;color:" + htmlColor(accent) + "'>" + eyebrow + "</span><br>"
                + "<b style='font-size:17px'>" + title + "</b><br>"
                + "<span style='font-size:10px;color:#BED5D0'>" + detail + "</span>"
                + "</body></html>");
        button.setPreferredSize(new Dimension(340, 72));
        UiTheme.styleButton(button, accent);
        button.setFont(AssetLoader.terminalFont(16f, Font.BOLD));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setToolTipText(detail);
        installMenuHover(button, accent);
        button.addActionListener(event -> {
            if (!confirmChoice(title, mode, loadSave)) {
                return;
            }
            dispose();
            onStartSelected.accept(new StartChoice(mode, loadSave));
        });
        return button;
    }

    private boolean confirmChoice(String title, GameMode mode, boolean loadSave) {
        Color accent = routeAccent(mode, loadSave);
        boolean[] accepted = {false};

        JDialog dialog = new JDialog(this, "Confirm Route", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel panel = glassPanel(new BorderLayout(14, 14), accent);
        panel.setPreferredSize(new Dimension(470, 260));

        JLabel heading = new JLabel(routeHeading(title, mode, loadSave), SwingConstants.CENTER);
        heading.setForeground(accent);
        heading.setFont(AssetLoader.terminalFont(20f, Font.BOLD));

        JLabel body = new JLabel("<html><body style='width:395px;text-align:center'>"
                + routeMessage(mode, loadSave)
                + "<br><br><span style='color:#9EB8B4'>Save: " + escape(saveStatus) + "</span>"
                + "</body></html>", SwingConstants.CENTER);
        body.setForeground(UiTheme.TEXT);
        body.setFont(AssetLoader.terminalFont(13f, Font.PLAIN));

        JPanel pills = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        pills.setOpaque(false);
        pills.add(UiTheme.pill(mode == GameMode.TUTORIAL ? "GUIDED" : "NORMAL", accent));
        pills.add(UiTheme.pill(loadSave ? "LOAD ROUTE" : "NEW ROUTE", loadSave ? UiTheme.BLUE : UiTheme.AMBER));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        actions.setOpaque(false);

        JButton proceed = new JButton(routeActionText(mode, loadSave));
        UiTheme.styleButton(proceed, accent);
        installMenuHover(proceed, accent);
        proceed.addActionListener(event -> {
            accepted[0] = true;
            dialog.dispose();
        });

        JButton cancel = new JButton("Batal");
        UiTheme.styleButton(cancel, UiTheme.TEXT_MUTED);
        installMenuHover(cancel, UiTheme.TEXT_MUTED);
        cancel.addActionListener(event -> dialog.dispose());

        actions.add(cancel);
        actions.add(proceed);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        center.add(body, BorderLayout.CENTER);
        center.add(pills, BorderLayout.SOUTH);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        return accepted[0];
    }

    private Color routeAccent(GameMode mode, boolean loadSave) {
        if (loadSave && saveStatus.toLowerCase().contains("no normal save")) {
            return UiTheme.WARNING;
        }
        if (mode == GameMode.TUTORIAL) {
            return UiTheme.BLUE;
        }
        return loadSave ? UiTheme.AMBER : UiTheme.ACCENT;
    }

    private String loadGameDetail() {
        if (saveStatus.toLowerCase().contains("no normal save")) {
            return "No save found. Mulai normal route baru jika lanjut.";
        }
        return saveStatus;
    }

    private String htmlColor(Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    private String routeHeading(String title, GameMode mode, boolean loadSave) {
        if (loadSave && saveStatus.toLowerCase().contains("no normal save")) {
            return "NO SAVE FOUND";
        }
        if (mode == GameMode.TUTORIAL) {
            return "START TUTORIAL";
        }
        return loadSave ? "LOAD NORMAL ROUTE" : "START NEW ROUTE";
    }

    private String routeMessage(GameMode mode, boolean loadSave) {
        if (mode == GameMode.TUTORIAL) {
            return "Tutorial selalu mulai dari step 1.<br>Save/load dimatikan agar rute belajar tetap bersih.";
        }
        if (loadSave && saveStatus.toLowerCase().contains("no normal save")) {
            return "Belum ada save normal untuk dimuat.<br>Jika lanjut, game akan masuk sebagai route normal baru.";
        }
        if (loadSave) {
            return "Progress normal terakhir akan dimuat.<br>Event horror normal aktif setelah desktop terbuka.";
        }
        return "Game Baru mulai dari awal.<br>Save lama baru tertimpa kalau kamu menekan Save di desktop.";
    }

    private String routeActionText(GameMode mode, boolean loadSave) {
        if (mode == GameMode.TUTORIAL) {
            return "Mulai Tutorial";
        }
        if (loadSave && saveStatus.toLowerCase().contains("no normal save")) {
            return "Mulai Tanpa Save";
        }
        return loadSave ? "Load Game" : "Mulai Game";
    }

    private String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private JPanel glassPanel(java.awt.LayoutManager layout, Color borderColor) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(5, 12, 16, 185));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(graphics);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        return panel;
    }


    private void installMenuHover(JButton button, Color accent) {
        Color normal = button.getBackground();
        Color hover = new Color(
                Math.min(255, normal.getRed() + accent.getRed() / 8),
                Math.min(255, normal.getGreen() + accent.getGreen() / 8),
                Math.min(255, normal.getBlue() + accent.getBlue() / 8)
        );
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                button.setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                button.setBackground(normal);
            }
        });
    }
}
