package com.phisinthedark.ui;

import com.phisinthedark.assets.AssetLoader;
import com.phisinthedark.database.DatabaseManager;
import com.phisinthedark.database.User;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class LoginMenu extends JFrame {
    private final Consumer<User> onLoginSuccess;
    private final DatabaseManager dbManager;
    private final BufferedImage background;

    public LoginMenu(Consumer<User> onLoginSuccess, DatabaseManager dbManager) {
        super("Phis in the Dark - Authentication");
        this.onLoginSuccess = onLoginSuccess;
        this.dbManager = dbManager;
        this.background = AssetLoader.loadImage("wallpaper_desktop.png");
        buildMenu();
    }

    private void buildMenu() {
        UiTheme.applyGlobalDefaults();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(500, 400));

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
        root.setPreferredSize(new Dimension(600, 400));
        root.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        JLabel title = new JLabel("SECURE LOGIN", SwingConstants.CENTER);
        title.setForeground(UiTheme.ACCENT);
        title.setFont(AssetLoader.terminalFont(32f, Font.BOLD));

        JPanel formPanel = glassPanel(new GridLayout(4, 1, 10, 10), UiTheme.ACCENT_DIM);
        formPanel.setPreferredSize(new Dimension(400, 180));

        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(UiTheme.TEXT);
        userLabel.setFont(AssetLoader.terminalFont(14f, Font.PLAIN));
        JTextField userField = new JTextField();
        userField.setBackground(UiTheme.PANEL);
        userField.setForeground(UiTheme.TEXT);
        userField.setCaretColor(UiTheme.ACCENT);
        userField.setBorder(BorderFactory.createLineBorder(UiTheme.ACCENT_DIM));

        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(UiTheme.TEXT);
        passLabel.setFont(AssetLoader.terminalFont(14f, Font.PLAIN));
        JPasswordField passField = new JPasswordField();
        passField.setBackground(UiTheme.PANEL);
        passField.setForeground(UiTheme.TEXT);
        passField.setCaretColor(UiTheme.ACCENT);
        passField.setBorder(BorderFactory.createLineBorder(UiTheme.ACCENT_DIM));

        formPanel.add(userLabel);
        formPanel.add(userField);
        formPanel.add(passLabel);
        formPanel.add(passField);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        actionPanel.setOpaque(false);

        JButton loginBtn = new JButton("Login");
        UiTheme.styleButton(loginBtn, UiTheme.ACCENT);
        loginBtn.addActionListener(e -> {
            String u = userField.getText();
            String p = new String(passField.getPassword());
            User user = dbManager.login(u, p);
            if (user != null) {
                dispose();
                onLoginSuccess.accept(user);
            } else {
                JOptionPane.showMessageDialog(this, "Login gagal. Username atau password salah.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton registerBtn = new JButton("Register");
        UiTheme.styleButton(registerBtn, UiTheme.BLUE);
        registerBtn.addActionListener(e -> {
            String u = userField.getText();
            String p = new String(passField.getPassword());
            if (u.isBlank() || p.isBlank()) {
                JOptionPane.showMessageDialog(this, "Username dan password tidak boleh kosong.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (dbManager.register(u, p, u)) {
                JOptionPane.showMessageDialog(this, "Registrasi berhasil! Silakan login.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Registrasi gagal. Username mungkin sudah ada.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        actionPanel.add(loginBtn);
        actionPanel.add(registerBtn);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);
        centerPanel.add(formPanel, BorderLayout.CENTER);
        centerPanel.add(actionPanel, BorderLayout.SOUTH);

        root.add(title, BorderLayout.NORTH);
        root.add(centerPanel, BorderLayout.CENTER);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
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
}
