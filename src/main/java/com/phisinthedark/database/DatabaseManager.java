package com.phisinthedark.database;

import com.phisinthedark.puzzle.Puzzle;
import com.phisinthedark.website.Browser;
import com.phisinthedark.website.Website;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

public class DatabaseManager {
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(".env")) {
            props.load(fis);
        } catch (IOException e) {
            // Silently ignore if .env file is not found, will fallback
        }

        String envUrl = props.getProperty("DB_URL", System.getenv("DB_URL"));
        String envUser = props.getProperty("DB_USER", System.getenv("DB_USER"));
        String envPassword = props.getProperty("DB_PASSWORD", System.getenv("DB_PASSWORD"));

        // Fallback to local config if environment variables are not set
        URL = (envUrl != null && !envUrl.isEmpty()) ? envUrl : "jdbc:mariadb://localhost:3306/phis_in_the_dark";
        USER = (envUser != null && !envUser.isEmpty()) ? envUser : "phis";
        PASSWORD = (envPassword != null) ? envPassword : "phis";

        // Explicitly load driver to prevent "No suitable driver found" error
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MariaDB JDBC Driver not found in classpath.");
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public User login(String username, String password) {
        String query = "SELECT id, username, display_name FROM users WHERE username = ? AND password_hash = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, hashPassword(password));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("id");
                    updateLastLogin(conn, userId);
                    return new User(userId, rs.getString("username"), rs.getString("display_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean register(String username, String password, String displayName) {
        String query = "INSERT INTO users (username, password_hash, display_name) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, hashPassword(password));
            stmt.setString(3, displayName);
            int rows = stmt.executeUpdate();
            
            // Also initialize user_settings
            if (rows > 0) {
                User user = login(username, password);
                if (user != null) {
                    try (PreparedStatement settingsStmt = conn.prepareStatement("INSERT INTO user_settings (user_id) VALUES (?)")) {
                        settingsStmt.setInt(1, user.id());
                        settingsStmt.executeUpdate();
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void updateLastLogin(Connection conn, int userId) {
        String query = "UPDATE users SET last_login_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void seedDataIfEmpty() {
        try (Connection conn = getConnection()) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM websites")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return; // Already seeded
                }
            }

            Browser defaultBrowser = Browser.createDefaultBrowser();
            
            String insertWebsite = "INSERT INTO websites (id, title, url, visual_style, subtitle, screenshot_asset, unlocked_file_title, unlocked_file_content, sort_order) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String insertBody = "INSERT INTO website_body_lines (website_id, line_order, content) VALUES (?, ?, ?)";
            String insertPuzzle = "INSERT INTO puzzles (id, website_id, title, concept, code_block, instruction, answer, tutorial_answer, reward_item, solved_message) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String insertHint = "INSERT INTO puzzle_hints (puzzle_id, hint_order, content) VALUES (?, ?, ?)";

            try (PreparedStatement siteStmt = conn.prepareStatement(insertWebsite);
                 PreparedStatement bodyStmt = conn.prepareStatement(insertBody);
                 PreparedStatement puzzleStmt = conn.prepareStatement(insertPuzzle);
                 PreparedStatement hintStmt = conn.prepareStatement(insertHint)) {

                for (Website site : defaultBrowser.getWebsites()) {
                    siteStmt.setString(1, site.getId());
                    siteStmt.setString(2, site.getTitle());
                    siteStmt.setString(3, site.getUrl());
                    siteStmt.setString(4, site.getVisualStyle().name());
                    siteStmt.setString(5, site.getSubtitle());
                    siteStmt.setString(6, site.getScreenshotAsset());
                    siteStmt.setString(7, site.getUnlockedFileTitle());
                    siteStmt.setString(8, site.getUnlockedFileContent());
                    siteStmt.setInt(9, site.getSortOrder());
                    siteStmt.executeUpdate();

                    int lineOrder = 1;
                    for (String line : site.getBodyLines()) {
                        bodyStmt.setString(1, site.getId());
                        bodyStmt.setInt(2, lineOrder++);
                        bodyStmt.setString(3, line);
                        bodyStmt.executeUpdate();
                    }

                    if (site.getPuzzle() != null) {
                        Puzzle p = site.getPuzzle();
                        puzzleStmt.setString(1, p.getId());
                        puzzleStmt.setString(2, site.getId());
                        puzzleStmt.setString(3, p.getTitle());
                        puzzleStmt.setString(4, p.getConcept());
                        puzzleStmt.setString(5, p.getCodeBlock());
                        puzzleStmt.setString(6, p.getInstruction());
                        puzzleStmt.setString(7, p.getAnswer());
                        puzzleStmt.setString(8, p.getTutorialAnswer());
                        puzzleStmt.setString(9, p.getRewardItem());
                        puzzleStmt.setString(10, p.getSolvedMessage());
                        puzzleStmt.executeUpdate();

                        int hintOrder = 1;
                        for (String hint : p.getHints()) {
                            hintStmt.setString(1, p.getId());
                            hintStmt.setInt(2, hintOrder++);
                            hintStmt.setString(3, hint);
                            hintStmt.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Browser loadBrowser() {
        List<Website> websites = new ArrayList<>();
        String siteQuery = "SELECT * FROM websites ORDER BY sort_order";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(siteQuery)) {

            while (rs.next()) {
                String siteId = rs.getString("id");
                List<String> bodyLines = loadBodyLines(conn, siteId);
                Puzzle puzzle = loadPuzzle(conn, siteId);

                websites.add(new Website(
                        siteId,
                        rs.getString("title"),
                        rs.getString("url"),
                        Website.VisualStyle.valueOf(rs.getString("visual_style")),
                        rs.getString("subtitle"),
                        rs.getString("screenshot_asset"),
                        bodyLines,
                        puzzle,
                        rs.getString("unlocked_file_title"),
                        rs.getString("unlocked_file_content"),
                        rs.getInt("sort_order")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Browser(websites);
    }

    private List<String> loadBodyLines(Connection conn, String websiteId) throws SQLException {
        List<String> lines = new ArrayList<>();
        String query = "SELECT content FROM website_body_lines WHERE website_id = ? ORDER BY line_order";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, websiteId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lines.add(rs.getString("content"));
                }
            }
        }
        return lines;
    }

    private Puzzle loadPuzzle(Connection conn, String websiteId) throws SQLException {
        String query = "SELECT * FROM puzzles WHERE website_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, websiteId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String puzzleId = rs.getString("id");
                    List<String> hints = loadHints(conn, puzzleId);
                    
                    return new DatabasePuzzle(
                            puzzleId,
                            rs.getString("title"),
                            rs.getString("concept"),
                            rs.getString("code_block"),
                            rs.getString("instruction"),
                            rs.getString("answer"),
                            rs.getString("tutorial_answer"),
                            rs.getString("solved_message"),
                            rs.getString("reward_item"),
                            hints
                    );
                }
            }
        }
        return null;
    }

    private List<String> loadHints(Connection conn, String puzzleId) throws SQLException {
        List<String> hints = new ArrayList<>();
        String query = "SELECT content FROM puzzle_hints WHERE puzzle_id = ? ORDER BY hint_order";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, puzzleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    hints.add(rs.getString("content"));
                }
            }
        }
        return hints;
    }
}
