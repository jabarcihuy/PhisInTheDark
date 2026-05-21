package com.phisinthedark.ui;

import com.phisinthedark.assets.AssetLoader;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class ImageWindow extends BaseWindow {
    public ImageWindow(String title, String imageFile, int width, int height) {
        super(title, width, height);
        BufferedImage image = AssetLoader.loadImage(imageFile);
        if (image == null) {
            JLabel missing = new JLabel("missing asset: " + imageFile, SwingConstants.CENTER);
            missing.setForeground(UiTheme.TEXT);
            missing.setFont(UiTheme.font(13f, java.awt.Font.BOLD));
            content.add(missing, BorderLayout.CENTER);
            return;
        }

        Image scaled = image.getScaledInstance(width - 28, height - 58, Image.SCALE_SMOOTH);
        JLabel label = new JLabel(new ImageIcon(scaled), SwingConstants.CENTER);
        label.setOpaque(false);
        label.setBorder(javax.swing.BorderFactory.createLineBorder(UiTheme.BORDER, 1));
        content.add(label, BorderLayout.CENTER);
    }
}
