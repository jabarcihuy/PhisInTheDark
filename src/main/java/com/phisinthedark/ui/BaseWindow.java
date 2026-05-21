package com.phisinthedark.ui;

import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

public abstract class BaseWindow extends JInternalFrame {
    protected static final Color PANEL_DARK = UiTheme.PANEL;
    protected static final Color TEXT_MAIN = UiTheme.TEXT;
    protected static final Color ACCENT = UiTheme.ACCENT;

    protected final JPanel content;
    private final Dimension initialSize;

    protected BaseWindow(String title, int width, int height) {
        super(title, true, true, true, true);
        UiTheme.applyGlobalDefaults();
        this.initialSize = new Dimension(width, height);
        this.content = new JPanel(new BorderLayout(10, 10));
        this.content.setBackground(PANEL_DARK);
        resetContentBorder();
        setContentPane(content);
        setPreferredSize(initialSize);
        setMinimumSize(new Dimension(Math.min(width, 420), Math.min(height, 280)));
        setSize(initialSize);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setFrameIcon(null);
        setFont(UiTheme.font(12f, java.awt.Font.PLAIN));
    }

    protected final void resetContentBorder() {
        this.content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(31, 49, 54), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
    }

    public final void prepareForDisplay() {
        if (getWidth() < initialSize.width || getHeight() < initialSize.height) {
            setSize(initialSize);
        }
        content.revalidate();
        content.doLayout();
        revalidate();
        repaint();
    }
}
