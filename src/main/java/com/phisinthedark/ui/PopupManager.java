package com.phisinthedark.ui;

import com.phisinthedark.assets.AssetLoader;

import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class PopupManager {
    private static final int MAX_UNKNOWN_USER_POPUPS = 2;

    private final DesktopUI desktopUI;
    private final JDesktopPane desktopPane;
    private final Random random;
    private final Set<JInternalFrame> unknownUserPopups;
    private long lastUnknownUserLimitNotice;
    private int popupCount;

    public PopupManager(DesktopUI desktopUI, JDesktopPane desktopPane) {
        this.desktopUI = desktopUI;
        this.desktopPane = desktopPane;
        this.random = new Random();
        this.unknownUserPopups = new HashSet<>();
    }

    public void showPopup(String title, String message) {
        boolean unknownUser = "UNKNOWN USER".equalsIgnoreCase(title);
        if (unknownUser) {
            pruneClosedUnknownUserPopups();
            if (unknownUserPopups.size() >= MAX_UNKNOWN_USER_POPUPS) {
                long now = System.currentTimeMillis();
                if (now - lastUnknownUserLimitNotice > 5000) {
                    lastUnknownUserLimitNotice = now;
                    desktopUI.showNotification("UNKNOWN USER", "Popup limit reached. Tutup salah satu popup dulu.");
                }
                return;
            }
        }

        JInternalFrame popup = new JInternalFrame(title, false, true, false, false);
        popup.setFrameIcon(null);
        popup.setSize(360, 168);
        popup.setLayout(new BorderLayout(8, 8));
        if (unknownUser) {
            unknownUserPopups.add(popup);
            popup.addInternalFrameListener(new InternalFrameAdapter() {
                @Override
                public void internalFrameClosed(InternalFrameEvent event) {
                    unknownUserPopups.remove(popup);
                }

                @Override
                public void internalFrameClosing(InternalFrameEvent event) {
                    unknownUserPopups.remove(popup);
                }
            });
        }

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(new Color(26, 18, 23));
        panel.setBorder(UiTheme.panelBorder(UiTheme.WARNING));

        BufferedImage popupImage = AssetLoader.loadImage("popup_warning.png");
        JLabel label = new JLabel("<html><body style='width:230px'>" + escape(message) + "</body></html>");
        if (popupImage != null) {
            Image scaled = popupImage.getScaledInstance(92, 52, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaled));
            label.setIconTextGap(10);
        }
        label.setForeground(new Color(245, 230, 234));
        label.setFont(AssetLoader.terminalFont(12f, Font.PLAIN));

        JButton close = new JButton("OK");
        UiTheme.styleButton(close, UiTheme.WARNING);
        close.addActionListener(event -> popup.dispose());
        desktopUI.applyButtonHover(close);

        panel.add(label, BorderLayout.CENTER);
        panel.add(close, BorderLayout.SOUTH);
        popup.add(panel);

        Point point = randomPoint(popup.getSize());
        popup.setLocation(point);
        desktopPane.add(popup, JDesktopPane.POPUP_LAYER);
        popup.setVisible(true);
        popup.toFront();
        popupCount++;
    }

    public void showToast(String title, String message) {
        JInternalFrame toast = new JInternalFrame(title, false, false, false, false);
        toast.setFrameIcon(null);
        toast.setSize(390, 96);

        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBackground(new Color(8, 29, 34));
        panel.setBorder(UiTheme.panelBorder(UiTheme.ACCENT));

        JLabel label = new JLabel("<html><b>" + escape(title) + "</b><br>" + escape(message) + "</html>");
        BufferedImage notificationImage = AssetLoader.loadImage("notification_asset.png");
        if (notificationImage != null) {
            Image scaled = notificationImage.getScaledInstance(72, 23, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaled));
            label.setIconTextGap(10);
        }
        label.setForeground(UiTheme.TEXT);
        label.setFont(AssetLoader.terminalFont(12f, Font.PLAIN));
        panel.add(label, BorderLayout.CENTER);
        toast.add(panel);

        int x = Math.max(10, desktopPane.getWidth() - toast.getWidth() - 24);
        int y = Math.max(10, desktopPane.getHeight() - toast.getHeight() - 24 - (popupCount % 3) * 104);
        toast.setLocation(x, y);
        desktopPane.add(toast, JDesktopPane.POPUP_LAYER);
        toast.setVisible(true);
        toast.toFront();
        popupCount++;

        Timer timer = new Timer(3600, event -> toast.dispose());
        timer.setRepeats(false);
        timer.start();
    }

    private Point randomPoint(Dimension size) {
        int maxX = Math.max(20, desktopPane.getWidth() - size.width - 20);
        int maxY = Math.max(20, desktopPane.getHeight() - size.height - 20);
        int x = 30 + random.nextInt(Math.max(1, maxX - 20));
        int y = 30 + random.nextInt(Math.max(1, maxY - 20));
        return new Point(x, y);
    }

    private void pruneClosedUnknownUserPopups() {
        unknownUserPopups.removeIf(frame -> frame.isClosed() || !frame.isDisplayable());
    }

    private String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
