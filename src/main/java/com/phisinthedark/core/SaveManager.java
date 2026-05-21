package com.phisinthedark.core;

import com.phisinthedark.audio.AudioManager;
import com.phisinthedark.database.DatabaseManager;
import com.phisinthedark.player.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class SaveManager {
    private final DatabaseManager dbManager;
    private final int userId;

    public SaveManager(DatabaseManager dbManager, int userId) {
        this.dbManager = dbManager;
        this.userId = userId;
    }

    public boolean save(Player player) {
        return save(player, GameMode.NORMAL, null);
    }

    public boolean save(Player player, GameMode mode) {
        return save(player, mode, null);
    }

    public boolean save(Player player, GameMode mode, AudioManager audioManager) {
        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Ensure save_slot exists
                int slotId = -1;
                String checkSlot = "SELECT id FROM save_slots WHERE user_id = ? AND mode = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSlot)) {
                    checkStmt.setInt(1, userId);
                    checkStmt.setString(2, mode.name());
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            slotId = rs.getInt("id");
                        }
                    }
                }

                if (slotId == -1) {
                    String insertSlot = "INSERT INTO save_slots (user_id, slot_name, mode, paranoia, ending_reached) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSlot, Statement.RETURN_GENERATED_KEYS)) {
                        insertStmt.setInt(1, userId);
                        insertStmt.setString(2, "Save " + mode.name());
                        insertStmt.setString(3, mode.name());
                        insertStmt.setInt(4, player.getParanoia());
                        insertStmt.setBoolean(5, player.isEndingReached());
                        insertStmt.executeUpdate();
                        try (ResultSet rs = insertStmt.getGeneratedKeys()) {
                            if (rs.next()) {
                                slotId = rs.getInt(1);
                            }
                        }
                    }
                } else {
                    String updateSlot = "UPDATE save_slots SET paranoia = ?, ending_reached = ?, last_played_at = CURRENT_TIMESTAMP WHERE id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSlot)) {
                        updateStmt.setInt(1, player.getParanoia());
                        updateStmt.setBoolean(2, player.isEndingReached());
                        updateStmt.setInt(3, slotId);
                        updateStmt.executeUpdate();
                    }
                }

                // 2. Clear old items and puzzles
                try (PreparedStatement delItems = conn.prepareStatement("DELETE FROM save_inventory_items WHERE save_slot_id = ?")) {
                    delItems.setInt(1, slotId);
                    delItems.executeUpdate();
                }
                try (PreparedStatement delPuzzles = conn.prepareStatement("DELETE FROM save_solved_puzzles WHERE save_slot_id = ?")) {
                    delPuzzles.setInt(1, slotId);
                    delPuzzles.executeUpdate();
                }

                // 3. Insert items
                String insertItem = "INSERT INTO save_inventory_items (save_slot_id, item_name) VALUES (?, ?)";
                try (PreparedStatement itemStmt = conn.prepareStatement(insertItem)) {
                    for (String item : player.getInventory().getItems()) {
                        itemStmt.setInt(1, slotId);
                        itemStmt.setString(2, item);
                        itemStmt.executeUpdate();
                    }
                }

                // 4. Insert puzzles
                String insertPuzzle = "INSERT INTO save_solved_puzzles (save_slot_id, puzzle_id) VALUES (?, ?)";
                try (PreparedStatement puzzleStmt = conn.prepareStatement(insertPuzzle)) {
                    for (String puzzleId : player.getSolvedPuzzleIds()) {
                        puzzleStmt.setInt(1, slotId);
                        puzzleStmt.setString(2, puzzleId);
                        puzzleStmt.executeUpdate();
                    }
                }

                // 5. Update user_settings
                if (audioManager != null) {
                    String updateSettings = "UPDATE user_settings SET audio_muted = ?, ambience_muted = ? WHERE user_id = ?";
                    try (PreparedStatement settingStmt = conn.prepareStatement(updateSettings)) {
                        settingStmt.setBoolean(1, audioManager.isMuted());
                        settingStmt.setBoolean(2, audioManager.isAmbienceMuted());
                        settingStmt.setInt(3, userId);
                        settingStmt.executeUpdate();
                    }
                }

                // Also update player name in users table (if they somehow change it)
                String updateName = "UPDATE users SET display_name = ? WHERE id = ?";
                try (PreparedStatement nameStmt = conn.prepareStatement(updateName)) {
                    nameStmt.setString(1, player.getName());
                    nameStmt.setInt(2, userId);
                    nameStmt.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean load(Player player) {
        return load(player, GameMode.NORMAL, null);
    }

    public boolean load(Player player, GameMode mode) {
        return load(player, mode, null);
    }

    public boolean load(Player player, GameMode mode, AudioManager audioManager) {
        try (Connection conn = dbManager.getConnection()) {
            
            // 1. Get slot id
            int slotId = -1;
            String checkSlot = "SELECT id, paranoia, ending_reached FROM save_slots WHERE user_id = ? AND mode = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSlot)) {
                checkStmt.setInt(1, userId);
                checkStmt.setString(2, mode.name());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        slotId = rs.getInt("id");
                        player.setParanoia(rs.getInt("paranoia"));
                        player.setEndingReached(rs.getBoolean("ending_reached"));
                    } else {
                        return false; // No save found
                    }
                }
            }

            // Load name
            String nameQuery = "SELECT display_name FROM users WHERE id = ?";
            try (PreparedStatement nameStmt = conn.prepareStatement(nameQuery)) {
                nameStmt.setInt(1, userId);
                try (ResultSet rs = nameStmt.executeQuery()) {
                    if (rs.next()) {
                        player.setName(rs.getString("display_name"));
                    }
                }
            }

            // Load items
            Set<String> items = new HashSet<>();
            String itemQuery = "SELECT item_name FROM save_inventory_items WHERE save_slot_id = ?";
            try (PreparedStatement itemStmt = conn.prepareStatement(itemQuery)) {
                itemStmt.setInt(1, slotId);
                try (ResultSet rs = itemStmt.executeQuery()) {
                    while (rs.next()) {
                        items.add(rs.getString("item_name"));
                    }
                }
            }
            player.getInventory().restoreItems(items);

            // Load puzzles
            Set<String> puzzles = new HashSet<>();
            String puzzleQuery = "SELECT puzzle_id FROM save_solved_puzzles WHERE save_slot_id = ?";
            try (PreparedStatement puzzleStmt = conn.prepareStatement(puzzleQuery)) {
                puzzleStmt.setInt(1, slotId);
                try (ResultSet rs = puzzleStmt.executeQuery()) {
                    while (rs.next()) {
                        puzzles.add(rs.getString("puzzle_id"));
                    }
                }
            }
            player.restoreSolvedPuzzles(migrateSolvedPuzzleIds(puzzles));

            // Load settings
            if (audioManager != null) {
                String settingQuery = "SELECT audio_muted, ambience_muted FROM user_settings WHERE user_id = ?";
                try (PreparedStatement settingStmt = conn.prepareStatement(settingQuery)) {
                    settingStmt.setInt(1, userId);
                    try (ResultSet rs = settingStmt.executeQuery()) {
                        if (rs.next()) {
                            audioManager.setMuted(rs.getBoolean("audio_muted"));
                            audioManager.setAmbienceMuted(rs.getBoolean("ambience_muted"));
                        }
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSave() {
        try (Connection conn = dbManager.getConnection()) {
            String delSlot = "DELETE FROM save_slots WHERE user_id = ? AND mode = ?";
            try (PreparedStatement delStmt = conn.prepareStatement(delSlot)) {
                delStmt.setInt(1, userId);
                delStmt.setString(2, GameMode.NORMAL.name());
                return delStmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String describeSave() {
        try (Connection conn = dbManager.getConnection()) {
            String query = "SELECT id, ending_reached FROM save_slots WHERE user_id = ? AND mode = ?";
            int slotId = -1;
            boolean ending = false;
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.setString(2, GameMode.NORMAL.name());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        slotId = rs.getInt("id");
                        ending = rs.getBoolean("ending_reached");
                    } else {
                        return "No normal save found.";
                    }
                }
            }
            
            if (ending) {
                return "Normal save: route complete.";
            }

            int puzzleCount = 0;
            String countQuery = "SELECT COUNT(*) FROM save_solved_puzzles WHERE save_slot_id = ?";
            try (PreparedStatement countStmt = conn.prepareStatement(countQuery)) {
                countStmt.setInt(1, slotId);
                try (ResultSet rs = countStmt.executeQuery()) {
                    if (rs.next()) {
                        puzzleCount = rs.getInt(1);
                    }
                }
            }
            return "Normal save: " + puzzleCount + "/3 keys.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Normal save: unreadable.";
        }
    }

    private Set<String> migrateSolvedPuzzleIds(Set<String> puzzleIds) {
        if (puzzleIds.remove("puzzle_three_knocks")) {
            puzzleIds.add("puzzle_signal_doubler");
        }
        return puzzleIds;
    }
}
