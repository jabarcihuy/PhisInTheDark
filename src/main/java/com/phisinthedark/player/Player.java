package com.phisinthedark.player;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Player {
    private String name;
    private final Inventory inventory;
    private final Set<String> solvedPuzzleIds;
    private int paranoia;
    private boolean endingReached;

    public Player(String name) {
        this.name = name;
        this.inventory = new Inventory();
        this.solvedPuzzleIds = new LinkedHashSet<>();
        this.paranoia = 0;
    }

    public boolean solvePuzzle(String puzzleId) {
        return solvedPuzzleIds.add(puzzleId);
    }

    public boolean hasSolved(String puzzleId) {
        return solvedPuzzleIds.contains(puzzleId);
    }

    public void restoreSolvedPuzzles(Collection<String> puzzleIds) {
        solvedPuzzleIds.clear();
        solvedPuzzleIds.addAll(puzzleIds);
    }

    public void clearProgress() {
        solvedPuzzleIds.clear();
        inventory.clear();
        paranoia = 0;
        endingReached = false;
    }

    public void increaseParanoia(int amount) {
        paranoia = Math.min(100, Math.max(0, paranoia + amount));
    }

    public int getSolvedPuzzleCount() {
        return solvedPuzzleIds.size();
    }

    public Set<String> getSolvedPuzzleIds() {
        return Collections.unmodifiableSet(solvedPuzzleIds);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public int getParanoia() {
        return paranoia;
    }

    public void setParanoia(int paranoia) {
        this.paranoia = Math.min(100, Math.max(0, paranoia));
    }

    public boolean isEndingReached() {
        return endingReached;
    }

    public void setEndingReached(boolean endingReached) {
        this.endingReached = endingReached;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }
    }
}
