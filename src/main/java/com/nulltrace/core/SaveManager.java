package com.nulltrace.core;

import com.nulltrace.player.Player;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class SaveManager {
    private final Path saveDirectory;

    public SaveManager() {
        this.saveDirectory = Path.of(System.getProperty("user.home"), ".nulltrace");
    }

    public boolean save(Player player) {
        return save(player, GameMode.NORMAL);
    }

    public boolean save(Player player, GameMode mode) {
        Path saveFile = getSaveFile(mode);
        try {
            Files.createDirectories(saveFile.getParent());

            Properties properties = new Properties();
            properties.setProperty("player.name", player.getName());
            properties.setProperty("solved", String.join(",", player.getSolvedPuzzleIds()));
            properties.setProperty("items", String.join(",", player.getInventory().getItems()));
            properties.setProperty("paranoia", Integer.toString(player.getParanoia()));
            properties.setProperty("ending", Boolean.toString(player.isEndingReached()));

            try (OutputStream output = Files.newOutputStream(saveFile)) {
                properties.store(output, "Phis in the Dark save data");
            }
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    public boolean load(Player player) {
        return load(player, GameMode.NORMAL);
    }

    public boolean load(Player player, GameMode mode) {
        Path saveFile = getSaveFile(mode);
        if (!Files.exists(saveFile)) {
            return false;
        }

        try (InputStream input = Files.newInputStream(saveFile)) {
            Properties properties = new Properties();
            properties.load(input);

            player.clearProgress();
            player.setName(properties.getProperty("player.name", player.getName()));
            player.restoreSolvedPuzzles(migrateSolvedPuzzleIds(parseCsv(properties.getProperty("solved", ""))));
            player.getInventory().restoreItems(parseCsv(properties.getProperty("items", "")));
            player.setParanoia(parseInteger(properties.getProperty("paranoia", "0")));
            player.setEndingReached(Boolean.parseBoolean(properties.getProperty("ending", "false")));
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    public boolean deleteSave() {
        try {
            return Files.deleteIfExists(getSaveFile(GameMode.NORMAL));
        } catch (IOException exception) {
            return false;
        }
    }

    public String describeSave() {
        Path saveFile = getSaveFile(GameMode.NORMAL);
        if (!Files.exists(saveFile)) {
            return "No normal save found.";
        }

        try (InputStream input = Files.newInputStream(saveFile)) {
            Properties properties = new Properties();
            properties.load(input);
            int solved = parseCsv(properties.getProperty("solved", "")).size();
            boolean ending = Boolean.parseBoolean(properties.getProperty("ending", "false"));
            if (ending) {
                return "Normal save: route complete.";
            }
            return "Normal save: " + solved + "/3 keys.";
        } catch (IOException exception) {
            return "Normal save: unreadable.";
        }
    }

    private Set<String> parseCsv(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    private Set<String> migrateSolvedPuzzleIds(Set<String> puzzleIds) {
        if (puzzleIds.remove("puzzle_three_knocks")) {
            puzzleIds.add("puzzle_signal_doubler");
        }
        return puzzleIds;
    }

    private int parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    public Path getSaveFile() {
        return getSaveFile(GameMode.NORMAL);
    }

    public Path getSaveFile(GameMode mode) {
        return saveDirectory.resolve("save.properties");
    }
}
