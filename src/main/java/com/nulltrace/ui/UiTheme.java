package com.nulltrace.ui;

import com.nulltrace.assets.AssetLoader;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;

public final class UiTheme {
    public static final Color BACKDROP = new Color(4, 7, 10);
    public static final Color PANEL = new Color(14, 19, 24);
    public static final Color PANEL_ALT = new Color(20, 28, 34);
    public static final Color FIELD = new Color(6, 10, 13);
    public static final Color ACCENT = new Color(49, 220, 176);
    public static final Color ACCENT_DIM = new Color(28, 92, 86);
    public static final Color WARNING = new Color(255, 87, 112);
    public static final Color AMBER = new Color(226, 194, 111);
    public static final Color BLUE = new Color(104, 220, 255);
    public static final Color TEXT = new Color(226, 244, 240);
    public static final Color TEXT_MUTED = new Color(158, 184, 180);
    public static final Color BORDER = new Color(42, 69, 72);

    private UiTheme() {
    }

    public static void applyGlobalDefaults() {
        UIManager.put("Panel.background", PANEL);
        UIManager.put("Button.font", font(12f, Font.BOLD));
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("Button.background", PANEL_ALT);
        UIManager.put("ComboBox.font", font(12f, Font.PLAIN));
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("ComboBox.background", FIELD);
        UIManager.put("Label.font", font(12f, Font.PLAIN));
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("ProgressBar.foreground", ACCENT);
        UIManager.put("ProgressBar.background", FIELD);
        UIManager.put("InternalFrame.activeTitleBackground", new Color(10, 28, 31));
        UIManager.put("InternalFrame.activeTitleForeground", TEXT);
        UIManager.put("InternalFrame.inactiveTitleBackground", new Color(10, 14, 18));
        UIManager.put("InternalFrame.inactiveTitleForeground", TEXT_MUTED);
    }

    public static Font font(float size, int style) {
        return AssetLoader.terminalFont(size, style);
    }

    public static Border pad(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }

    public static Border panelBorder(Color accent) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );
    }

    public static Border fieldBorder(Color accent) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        );
    }

    public static void styleButton(AbstractButton button) {
        styleButton(button, ACCENT);
    }

    public static void styleButton(AbstractButton button, Color accent) {
        button.setFont(font(12f, Font.BOLD));
        button.setForeground(TEXT);
        button.setBackground(PANEL_ALT);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 1),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)
        ));
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(0, 0, 0, 0));
    }

    public static void styleToolButton(AbstractButton button) {
        button.setFont(font(11f, Font.BOLD));
        button.setForeground(TEXT);
        button.setBackground(new Color(18, 27, 33));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(37, 66, 69), 1),
                BorderFactory.createEmptyBorder(5, 9, 5, 9)
        ));
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(0, 0, 0, 0));
    }

    public static void styleTextComponent(JTextComponent component) {
        styleTextComponent(component, ACCENT_DIM);
    }

    public static void styleTextComponent(JTextComponent component, Color accent) {
        component.setFont(font(13f, Font.PLAIN));
        component.setBackground(FIELD);
        component.setForeground(TEXT);
        component.setCaretColor(ACCENT);
        component.setBorder(fieldBorder(accent));
    }

    public static void styleAddressField(JTextField field) {
        styleTextComponent(field, BORDER);
        field.setEditable(false);
        field.setForeground(new Color(196, 226, 220));
    }

    public static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(font(12f, Font.BOLD));
        comboBox.setForeground(TEXT);
        comboBox.setBackground(FIELD);
        comboBox.setBorder(BorderFactory.createLineBorder(BORDER, 1));
    }

    public static JScrollPane wrapScroll(JTextComponent component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(30, 48, 52), 1));
        scrollPane.getViewport().setBackground(component.getBackground());
        return scrollPane;
    }

    public static JLabel pill(String text, Color accent) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setFont(font(11f, Font.BOLD));
        label.setForeground(TEXT);
        label.setBackground(new Color(
                Math.max(0, accent.getRed() / 7),
                Math.max(0, accent.getGreen() / 7),
                Math.max(0, accent.getBlue() / 7)
        ));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        return label;
    }

    public static void styleProgress(JProgressBar progressBar, Color accent) {
        progressBar.setForeground(accent);
        progressBar.setBackground(FIELD);
        progressBar.setBorder(BorderFactory.createLineBorder(new Color(35, 58, 62), 1));
        progressBar.setStringPainted(true);
        progressBar.setFont(font(11f, Font.BOLD));
    }
}
