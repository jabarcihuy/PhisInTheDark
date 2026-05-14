package com.nulltrace.assets;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Random;

public final class AssetGenerator {
    private static final Path ASSETS = Path.of("assets");
    private static final Path IMAGES = ASSETS.resolve("images");
    private static final Path SOUNDS = ASSETS.resolve("sounds");
    private static final Path FONTS = ASSETS.resolve("fonts");
    private static final int SAMPLE_RATE = 22_050;
    private static boolean ensured;

    private AssetGenerator() {
    }

    public static synchronized void ensureAssets() {
        if (ensured) {
            return;
        }

        try {
            Files.createDirectories(IMAGES);
            Files.createDirectories(SOUNDS);
            Files.createDirectories(FONTS);
            generateImages(false);
            generateSounds(false);
            copyFontsBestEffort(false);
            ensured = true;
        } catch (Throwable exception) {
            System.err.println("Asset generation failed: " + exception.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        System.setProperty("java.awt.headless", "true");
        boolean force = args.length > 0 && "--force".equalsIgnoreCase(args[0]);
        Files.createDirectories(IMAGES);
        Files.createDirectories(SOUNDS);
        Files.createDirectories(FONTS);
        generateImages(force);
        generateSounds(force);
        copyFontsBestEffort(force);
        System.out.println("Phis in the Dark assets generated in " + ASSETS.toAbsolutePath());
    }

    private static void copyFontsBestEffort(boolean force) {
        try {
            copyFonts(force);
        } catch (IOException exception) {
            System.err.println("Font asset generation skipped: " + exception.getMessage());
        }
    }

    private static void generateImages(boolean force) throws IOException {
        writePng("wallpaper_desktop.png", 1280, 720, force, AssetGenerator::paintWallpaper);
        writePng("terminal_background.png", 900, 560, force, AssetGenerator::paintTerminalBackground);
        writePng("loading_screen.png", 900, 506, force, AssetGenerator::paintLoadingScreen);
        writePng("popup_warning.png", 480, 270, force, AssetGenerator::paintPopupWarning);
        writePng("browser_ui_asset.png", 960, 600, force, AssetGenerator::paintBrowserUi);
        writePng("glitch_overlay.png", 1280, 720, force, AssetGenerator::paintGlitchOverlay);
        writePng("crt_overlay.png", 1280, 720, force, AssetGenerator::paintCrtOverlay);
        writePng("fake_error_screen.png", 900, 506, force, AssetGenerator::paintFakeErrorScreen);
        writePng("notification_asset.png", 480, 150, force, AssetGenerator::paintNotification);
        writePng("button_normal.png", 260, 72, force, (g, w, h) -> paintButton(g, w, h, new Color(21, 41, 44), new Color(49, 220, 176), "BUTTON"));
        writePng("button_hover.png", 260, 72, force, (g, w, h) -> paintButton(g, w, h, new Color(34, 74, 76), new Color(115, 255, 219), "BUTTON"));
        writePng("button_pressed.png", 260, 72, force, (g, w, h) -> paintButton(g, w, h, new Color(54, 18, 30), new Color(255, 87, 112), "BUTTON"));

        writePng("icon_browser.png", 128, 128, force, (g, w, h) -> paintIcon(g, w, h, "B", new Color(49, 220, 176)));
        writePng("icon_terminal.png", 128, 128, force, (g, w, h) -> paintIcon(g, w, h, ">", new Color(91, 255, 134)));
        writePng("icon_notes.png", 128, 128, force, (g, w, h) -> paintIcon(g, w, h, "N", new Color(226, 194, 111)));
        writePng("icon_mail.png", 128, 128, force, (g, w, h) -> paintIcon(g, w, h, "@", new Color(130, 190, 255)));
        writePng("icon_save.png", 128, 128, force, (g, w, h) -> paintIcon(g, w, h, "S", new Color(49, 220, 176)));
        writePng("icon_load.png", 128, 128, force, (g, w, h) -> paintIcon(g, w, h, "L", new Color(255, 87, 112)));
        writePng("icon_settings.png", 128, 128, force, (g, w, h) -> paintIcon(g, w, h, "*", new Color(104, 220, 255)));

        for (WebsiteAsset asset : websiteAssets()) {
            writePng(asset.fileName(), 960, 600, force, (g, w, h) -> paintWebsite(g, w, h, asset));
        }
    }

    private static void generateSounds(boolean force) throws IOException {
        writeWav("typing.wav", 0.08, force, (time, random) -> envelope(time, 0.08) * square(920, time) * 0.28);
        writeWav("notification.wav", 0.45, force, (time, random) -> {
            double first = Math.sin(twoPi(740) * time) * envelope(time, 0.22);
            double second = Math.sin(twoPi(980) * Math.max(0, time - 0.18)) * envelope(Math.max(0, time - 0.18), 0.24);
            return (first + second) * 0.22;
        });
        writeWav("glitch.wav", 0.65, force, (time, random) -> {
            double bit = random.nextDouble() > 0.5 ? 1 : -1;
            double tone = Math.sin(twoPi(55 + random.nextInt(900)) * time);
            return (bit * 0.26 + tone * 0.12) * envelope(time, 0.65);
        });
        writeWav("ambience_loop.wav", 5.0, force, (time, random) -> {
            double hum = Math.sin(twoPi(54) * time) * 0.09 + Math.sin(twoPi(111) * time) * 0.035;
            double noise = (random.nextDouble() * 2 - 1) * 0.025;
            return hum + noise;
        });
        writeWav("static_noise.wav", 1.4, force, (time, random) -> (random.nextDouble() * 2 - 1) * 0.23 * envelope(time, 1.4));
        writeWav("error.wav", 0.55, force, (time, random) -> {
            double frequency = 360 - (time * 260);
            return Math.sin(twoPi(frequency) * time) * 0.28 * envelope(time, 0.55);
        });
        writeWav("button_click.wav", 0.06, force, (time, random) -> Math.sin(twoPi(1250) * time) * 0.24 * envelope(time, 0.06));
        writeWav("creepy_whisper.wav", 3.2, force, (time, random) -> {
            double breath = (random.nextDouble() * 2 - 1) * 0.08;
            double pulse = Math.sin(twoPi(3.2) * time) * 0.5 + 0.5;
            double hiss = Math.sin(twoPi(1400 + random.nextInt(150)) * time) * 0.015;
            return (breath * pulse + hiss) * envelope(time, 3.2);
        });
    }

    private static void copyFonts(boolean force) throws IOException {
        copyFirstExisting("nulltrace-terminal.ttf", force, List.of(
                Path.of("/usr/share/fonts/Adwaita/AdwaitaMono-Regular.ttf"),
                Path.of("/usr/share/fonts/TTF/DejaVuSansMono.ttf"),
                Path.of("/usr/share/fonts/truetype/dejavu/DejaVuSansMono.ttf")
        ));
        copyFirstExisting("nulltrace-hacker.ttf", force, List.of(
                Path.of("/usr/share/fonts/Adwaita/AdwaitaMono-Bold.ttf"),
                Path.of("/usr/share/fonts/TTF/DejaVuSansMono-Bold.ttf"),
                Path.of("/usr/share/fonts/truetype/dejavu/DejaVuSansMono-Bold.ttf")
        ));
        copyFirstExisting("nulltrace-retro.ttf", force, List.of(
                Path.of("/usr/share/fonts/TTF/VeraMono.ttf"),
                Path.of("/usr/share/fonts/TTF/DejaVuSansMono-Oblique.ttf"),
                Path.of("/usr/share/fonts/Adwaita/AdwaitaMono-Italic.ttf")
        ));
    }

    private static void copyFirstExisting(String outputFile, boolean force, List<Path> candidates) throws IOException {
        Path output = FONTS.resolve(outputFile);
        if (!force && Files.exists(output) && Files.size(output) > 0) {
            return;
        }
        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                Files.copy(candidate, output, StandardCopyOption.REPLACE_EXISTING);
                return;
            }
        }
        throw new IOException("No local font found for " + outputFile);
    }

    private static void writePng(String fileName, int width, int height, boolean force, ImagePainter painter) throws IOException {
        Path output = IMAGES.resolve(fileName);
        if (!force && Files.exists(output) && Files.size(output) > 0) {
            return;
        }
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        painter.paint(g, width, height);
        g.dispose();
        ImageIO.write(image, "png", output.toFile());
    }

    private static void writeWav(String fileName, double seconds, boolean force, SoundPainter painter) throws IOException {
        Path output = SOUNDS.resolve(fileName);
        if (!force && Files.exists(output) && Files.size(output) > 0) {
            return;
        }

        int sampleCount = (int) (SAMPLE_RATE * seconds);
        byte[] data = new byte[sampleCount * 2];
        Random random = new Random(fileName.hashCode());
        for (int i = 0; i < sampleCount; i++) {
            double time = i / (double) SAMPLE_RATE;
            double sample = Math.max(-1, Math.min(1, painter.sample(time, random)));
            short pcm = (short) (sample * Short.MAX_VALUE);
            data[i * 2] = (byte) (pcm & 0xff);
            data[i * 2 + 1] = (byte) ((pcm >> 8) & 0xff);
        }

        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
        try (ByteArrayInputStream bytes = new ByteArrayInputStream(data);
             AudioInputStream audio = new AudioInputStream(bytes, format, sampleCount)) {
            AudioSystem.write(audio, AudioFileFormat.Type.WAVE, output.toFile());
        }
    }

    private static void paintWallpaper(Graphics2D g, int width, int height) {
        g.setPaint(new GradientPaint(0, 0, new Color(3, 8, 12), 0, height, new Color(21, 39, 35)));
        g.fillRect(0, 0, width, height);
        drawGrid(g, width, height, new Color(49, 220, 176, 36), 44, 36);
        drawNoise(g, width, height, 900, new Color(110, 255, 206, 35), 5);
        drawCrtLines(g, width, height, 36);

        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 76));
        g.setColor(new Color(49, 220, 176, 100));
        g.drawString("PHIS IN THE DARK", 92, 170);
        g.setColor(new Color(255, 87, 112, 75));
        g.drawString("PHIS IN THE DARK", 98, 176);

        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
        g.setColor(new Color(220, 245, 240, 150));
        g.drawString("fake operating system // beginner logic lab", 98, 210);
        g.drawString("status: signal cold, route hidden, trace unresolved", 98, 242);

        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(49, 220, 176, 70));
        g.drawRect(84, 112, 470, 160);
        g.setColor(new Color(255, 87, 112, 55));
        g.drawLine(720, 0, 960, height);
        g.drawLine(980, 0, 840, height);
    }

    private static void paintTerminalBackground(Graphics2D g, int width, int height) {
        g.setPaint(new GradientPaint(0, 0, new Color(2, 5, 6), width, height, new Color(7, 28, 18)));
        g.fillRect(0, 0, width, height);
        drawCrtLines(g, width, height, 44);
        drawNoise(g, width, height, 420, new Color(49, 220, 176, 30), 3);
        g.setColor(new Color(49, 220, 176, 48));
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
        for (int y = 58; y < height; y += 78) {
            g.drawString("null@trace:~$ █", 36, y);
        }
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRect(0, 0, width, height);
    }

    private static void paintLoadingScreen(Graphics2D g, int width, int height) {
        g.setPaint(new GradientPaint(0, 0, new Color(4, 8, 12), 0, height, new Color(14, 27, 31)));
        g.fillRect(0, 0, width, height);
        drawGrid(g, width, height, new Color(49, 220, 176, 24), 60, 60);
        g.setColor(new Color(49, 220, 176));
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 44));
        centerText(g, "PHIS IN THE DARK", width, 190);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
        centerText(g, "mounting fake desktop...", width, 232);
        g.setColor(new Color(20, 30, 33));
        g.fillRoundRect(240, 285, 420, 26, 12, 12);
        g.setColor(new Color(49, 220, 176));
        g.fillRoundRect(244, 289, 268, 18, 9, 9);
        g.setColor(new Color(255, 87, 112, 120));
        g.drawString("warning: page may remember input", 284, 342);
        drawCrtLines(g, width, height, 32);
    }

    private static void paintPopupWarning(Graphics2D g, int width, int height) {
        g.setColor(new Color(18, 12, 17));
        g.fillRect(0, 0, width, height);
        g.setStroke(new BasicStroke(4));
        g.setColor(new Color(255, 87, 112));
        g.drawRoundRect(10, 10, width - 20, height - 20, 18, 18);
        g.setColor(new Color(255, 87, 112, 35));
        g.fillRect(18, 58, width - 36, 8);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 32));
        g.setColor(new Color(255, 87, 112));
        g.drawString("UNKNOWN USER", 48, 78);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
        g.setColor(new Color(240, 230, 234));
        g.drawString("kamu masih membaca variable yang salah?", 48, 130);
        g.drawString("close?  [ OK ]", 48, 178);
        drawCrtLines(g, width, height, 45);
    }

    private static void paintBrowserUi(Graphics2D g, int width, int height) {
        g.setColor(new Color(8, 12, 16));
        g.fillRect(0, 0, width, height);
        drawWindow(g, 36, 34, width - 72, height - 68, new Color(13, 21, 24), new Color(49, 220, 176));
        g.setColor(new Color(18, 31, 35));
        g.fillRect(62, 76, width - 124, 44);
        g.setColor(new Color(49, 220, 176));
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
        g.drawString("null://deep.search", 86, 104);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 34));
        g.drawString("MYSTERIOUS INTERNET", 86, 184);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        g.setColor(new Color(230, 245, 241));
        g.drawString("forum / wiki / market / chat / archive", 90, 226);
        g.setColor(new Color(255, 87, 112, 95));
        g.fillRect(82, 284, width - 164, 12);
        drawNoise(g, width, height, 600, new Color(255, 87, 112, 50), 3);
    }

    private static void paintGlitchOverlay(Graphics2D g, int width, int height) {
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, width, height);
        g.setComposite(AlphaComposite.SrcOver);
        Random random = new Random(1349);
        for (int i = 0; i < 80; i++) {
            int y = random.nextInt(height);
            int h = 2 + random.nextInt(16);
            int x = random.nextInt(140) - 70;
            Color color = random.nextBoolean() ? new Color(49, 220, 176, 70) : new Color(255, 87, 112, 80);
            g.setColor(color);
            g.fillRect(x, y, width, h);
        }
    }

    private static void paintCrtOverlay(Graphics2D g, int width, int height) {
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, width, height);
        g.setComposite(AlphaComposite.SrcOver);
        drawCrtLines(g, width, height, 42);
        g.setColor(new Color(49, 220, 176, 15));
        g.fillRect(0, 0, width, height);
    }

    private static void paintFakeErrorScreen(Graphics2D g, int width, int height) {
        g.setColor(new Color(33, 4, 12));
        g.fillRect(0, 0, width, height);
        drawGrid(g, width, height, new Color(255, 87, 112, 30), 48, 48);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 44));
        g.setColor(new Color(255, 87, 112));
        centerText(g, "TRACE INTERRUPTED", width, 180);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
        centerText(g, "browser render warning: page remembers old input", width, 230);
        centerText(g, "press reload, then inspect the branch", width, 268);
        drawCrtLines(g, width, height, 50);
    }

    private static void paintNotification(Graphics2D g, int width, int height) {
        g.setColor(new Color(7, 32, 37));
        g.fillRoundRect(0, 0, width, height, 20, 20);
        g.setStroke(new BasicStroke(2));
        g.setColor(new Color(49, 220, 176));
        g.drawRoundRect(2, 2, width - 4, height - 4, 20, 20);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 22));
        g.drawString("SYSTEM NOTE", 28, 52);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        g.setColor(new Color(230, 250, 244));
        g.drawString("trace note: inspect the branch", 28, 92);
    }

    private static void paintButton(Graphics2D g, int width, int height, Color fill, Color accent, String label) {
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, width, height);
        g.setColor(fill);
        g.fillRoundRect(8, 8, width - 16, height - 16, 12, 12);
        g.setColor(accent);
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(8, 8, width - 16, height - 16, 12, 12);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        centerText(g, label, width, 46);
    }

    private static void paintIcon(Graphics2D g, int width, int height, String mark, Color accent) {
        g.setColor(new Color(9, 14, 17));
        g.fillRoundRect(10, 10, width - 20, height - 20, 18, 18);
        g.setColor(accent);
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(10, 10, width - 20, height - 20, 18, 18);
        g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 45));
        g.fillOval(22, 22, width - 44, height - 44);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 52));
        g.setColor(accent);
        centerText(g, mark, width, 80);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        centerText(g, "NULL", width, 104);
    }

    private static void paintWebsite(Graphics2D g, int width, int height, WebsiteAsset asset) {
        g.setPaint(new GradientPaint(0, 0, asset.top(), 0, height, asset.bottom()));
        g.fillRect(0, 0, width, height);
        drawGrid(g, width, height, new Color(asset.accent().getRed(), asset.accent().getGreen(), asset.accent().getBlue(), 22), asset.grid(), asset.grid());
        drawWindow(g, 54, 44, width - 108, height - 88, new Color(10, 14, 18, 205), asset.accent());

        g.setColor(new Color(20, 28, 32));
        g.fill(new RoundRectangle2D.Double(86, 82, width - 172, 42, 12, 12));
        g.setColor(asset.accent());
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 17));
        g.drawString(asset.url(), 106, 110);

        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 34));
        g.drawString(asset.title(), 86, 178);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        g.setColor(new Color(226, 240, 235));
        int y = 226;
        for (String line : asset.lines()) {
            g.drawString(line, 92, y);
            y += 38;
        }

        if (asset.corrupted()) {
            Random random = new Random(asset.fileName().hashCode());
            for (int i = 0; i < 18; i++) {
                g.setColor(random.nextBoolean()
                        ? new Color(255, 87, 112, 80)
                        : new Color(49, 220, 176, 70));
                g.fillRect(66 + random.nextInt(width - 132), 160 + random.nextInt(height - 240), 120 + random.nextInt(240), 5 + random.nextInt(16));
            }
        }

        g.setColor(new Color(255, 87, 112, 70));
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        g.drawString(asset.footer(), 92, height - 92);
        drawCrtLines(g, width, height, 26);
    }

    private static void drawWindow(Graphics2D g, int x, int y, int width, int height, Color fill, Color accent) {
        g.setColor(fill);
        g.fillRoundRect(x, y, width, height, 16, 16);
        g.setStroke(new BasicStroke(2));
        g.setColor(accent);
        g.drawRoundRect(x, y, width, height, 16, 16);
        g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 36));
        g.fillRect(x + 2, y + 34, width - 4, 2);
        g.setColor(new Color(255, 87, 112));
        g.fillOval(x + 18, y + 14, 10, 10);
        g.setColor(new Color(226, 194, 111));
        g.fillOval(x + 36, y + 14, 10, 10);
        g.setColor(new Color(49, 220, 176));
        g.fillOval(x + 54, y + 14, 10, 10);
    }

    private static void drawGrid(Graphics2D g, int width, int height, Color color, int xStep, int yStep) {
        g.setColor(color);
        for (int x = 0; x < width; x += xStep) {
            g.drawLine(x, 0, x, height);
        }
        for (int y = 0; y < height; y += yStep) {
            g.drawLine(0, y, width, y);
        }
    }

    private static void drawCrtLines(Graphics2D g, int width, int height, int alpha) {
        g.setColor(new Color(0, 0, 0, alpha));
        for (int y = 0; y < height; y += 4) {
            g.drawLine(0, y, width, y);
        }
    }

    private static void drawNoise(Graphics2D g, int width, int height, int count, Color color, int size) {
        Random random = new Random(width * 31L + height * 17L + count);
        g.setColor(color);
        for (int i = 0; i < count; i++) {
            g.fillRect(random.nextInt(width), random.nextInt(height), 1 + random.nextInt(size), 1);
        }
    }

    private static void centerText(Graphics2D g, String text, int width, int y) {
        int x = (width - g.getFontMetrics().stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    private static double square(double frequency, double time) {
        return Math.sin(twoPi(frequency) * time) > 0 ? 1 : -1;
    }

    private static double twoPi(double frequency) {
        return Math.PI * 2 * frequency;
    }

    private static double envelope(double time, double duration) {
        double attack = Math.min(1.0, time / Math.max(0.01, duration * 0.12));
        double release = Math.min(1.0, (duration - time) / Math.max(0.01, duration * 0.28));
        return Math.max(0, Math.min(attack, release));
    }

    private static List<WebsiteAsset> websiteAssets() {
        return List.of(
                new WebsiteAsset("website_hacker_forum.png", "BLACK LANTERN FORUM", "null://forum.black-lantern", new Color(49, 220, 176), new Color(7, 16, 18), new Color(13, 39, 34), 42, false, "[USER_13] password dibaca dari variable", "[root_mirror] akses bukan tebakan", "[guest] halaman ini bicara balik", "thread archived at 03:13"),
                new WebsiteAsset("website_hidden_wiki.png", "MOTH INDEX", "null://hidden.moth-index", new Color(226, 194, 111), new Color(21, 18, 12), new Color(42, 35, 20), 54, false, "Artikel 91: if memilih satu jalan", "Artikel 92: route dingin menuju server", "Artikel 93: jangan balas semua pesan", "index version: cold"),
                new WebsiteAsset("website_encrypted_login.png", "ENCRYPTED LOGIN", "null://gate.cold-login", new Color(140, 170, 255), new Color(7, 9, 22), new Color(20, 22, 45), 38, false, "username: ______", "password: value from memory", "two attempts left", "auth layer unstable"),
                new WebsiteAsset("website_creepy_marketplace.png", "ECHO MARKET", "null://market.echo-corrupt", new Color(255, 87, 112), new Color(22, 9, 16), new Color(43, 17, 28), 46, true, "Item #000: signal starts at 1", "Item #013: value doubles", "Buyer: unknown_user", "cart repeats forever"),
                new WebsiteAsset("website_abandoned_chatroom.png", "ROOM 0X13", "null://chat.room-013", new Color(104, 220, 255), new Color(6, 13, 20), new Color(13, 28, 42), 36, false, "03:12 <mira> kamu dengar static?", "03:13 <null> jangan ketik namamu", "03:14 <you> ...", "chat disconnected"),
                new WebsiteAsset("website_corrupted_website.png", "BROKEN MIRROR", "null://mirror.corrupted", new Color(255, 87, 112), new Color(24, 5, 11), new Color(15, 20, 25), 28, true, "ERR_RENDER_451", "body remembers previous input", "loop not closed", "repair impossible"),
                new WebsiteAsset("website_mysterious_blog.png", "THIRTEEN NOTES", "null://blog.thirteen-notes", new Color(190, 255, 170), new Color(10, 17, 12), new Color(24, 39, 24), 58, false, "Post: belajar logic pelan", "Draft: variable adalah kotak bernama", "Comment: jangan buat puzzle panjang", "last saved: never"),
                new WebsiteAsset("website_government_archive.png", "ARCHIVE 404", "null://gov.archive-404", new Color(210, 226, 240), new Color(14, 19, 23), new Color(29, 34, 38), 50, false, "Document NT-001 redacted", "Case: amateur signal intrusion", "Access: educational exception", "seal invalid"),
                new WebsiteAsset("website_conspiracy_forum.png", "WIRE ROOM", "null://forum.wire-room", new Color(255, 164, 92), new Color(26, 15, 7), new Color(44, 27, 13), 44, false, "Theory: popups are messages", "Proof: wallpaper changed after solve", "Thread: terminal exit command", "moderator missing"),
                new WebsiteAsset("website_deep_web_search.png", "NULL SEARCH", "null://search.deep-null", new Color(49, 220, 176), new Color(4, 10, 12), new Color(12, 28, 31), 32, true, "query: beginner logic horror", "result 1: black lantern forum", "result 2: moth index", "result 3: echo market")
        );
    }

    @FunctionalInterface
    private interface ImagePainter {
        void paint(Graphics2D graphics, int width, int height);
    }

    @FunctionalInterface
    private interface SoundPainter {
        double sample(double time, Random random);
    }

    private record WebsiteAsset(String fileName,
                                String title,
                                String url,
                                Color accent,
                                Color top,
                                Color bottom,
                                int grid,
                                boolean corrupted,
                                String line1,
                                String line2,
                                String line3,
                                String footer) {
        List<String> lines() {
            return List.of(line1, line2, line3);
        }
    }
}
