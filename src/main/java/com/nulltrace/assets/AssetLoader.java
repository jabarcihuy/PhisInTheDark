package com.nulltrace.assets;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AssetLoader {
    private static final Path ASSETS = Path.of("assets");
    private static Font cachedTerminalFont;

    private AssetLoader() {
    }

    public static Path imagePath(String fileName) {
        return ASSETS.resolve("images").resolve(fileName);
    }

    public static Path soundPath(String fileName) {
        return ASSETS.resolve("sounds").resolve(fileName);
    }

    public static BufferedImage loadImage(String fileName) {
        Path path = imagePath(fileName);
        if (!Files.exists(path)) {
            return null;
        }
        try {
            return ImageIO.read(path.toFile());
        } catch (IOException exception) {
            return null;
        }
    }

    public static ImageIcon loadIcon(String fileName, int width, int height) {
        BufferedImage image = loadImage(fileName);
        if (image == null) {
            return null;
        }
        Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    public static Font terminalFont(float size, int style) {
        Font base = getTerminalFont();
        return base.deriveFont(style, size);
    }

    private static Font getTerminalFont() {
        if (cachedTerminalFont != null) {
            return cachedTerminalFont;
        }

        Path fontPath = ASSETS.resolve("fonts").resolve("nulltrace-terminal.ttf");
        if (Files.exists(fontPath)) {
            try (InputStream input = Files.newInputStream(fontPath)) {
                cachedTerminalFont = Font.createFont(Font.TRUETYPE_FONT, input);
                return cachedTerminalFont;
            } catch (Exception ignored) {
                cachedTerminalFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
                return cachedTerminalFont;
            }
        }

        cachedTerminalFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        return cachedTerminalFont;
    }
}
