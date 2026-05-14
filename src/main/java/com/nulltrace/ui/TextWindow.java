package com.nulltrace.ui;

import com.nulltrace.assets.AssetLoader;

import javax.swing.JTextArea;
import java.awt.Font;

public class TextWindow extends BaseWindow {
    public TextWindow(String title, String text) {
        super(title, 460, 340);
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(AssetLoader.terminalFont(13f, Font.PLAIN));
        area.setBackground(UiTheme.FIELD);
        area.setForeground(TEXT_MAIN);
        area.setCaretColor(ACCENT);
        area.setBorder(UiTheme.pad(10, 10, 10, 10));
        content.add(UiTheme.wrapScroll(area));
    }
}
