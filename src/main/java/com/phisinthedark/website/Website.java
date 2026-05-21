package com.phisinthedark.website;

import com.phisinthedark.puzzle.Puzzle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Website {
    public enum VisualStyle {
        FORUM,
        WIKI,
        ENCRYPTED,
        MARKET,
        CHAT,
        CORRUPTED,
        BLOG,
        ARCHIVE,
        CONSPIRACY,
        SEARCH
    }

    private final String id;
    private final String title;
    private final String url;
    private final VisualStyle visualStyle;
    private final String subtitle;
    private final String screenshotAsset;
    private final List<String> bodyLines;
    private final Puzzle puzzle;
    private final String unlockedFileTitle;
    private final String unlockedFileContent;

    public Website(String id,
                   String title,
                   String url,
                   VisualStyle visualStyle,
                   String subtitle,
                   String screenshotAsset,
                   List<String> bodyLines,
                   Puzzle puzzle,
                   String unlockedFileTitle,
                   String unlockedFileContent) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.visualStyle = visualStyle;
        this.subtitle = subtitle;
        this.screenshotAsset = screenshotAsset;
        this.bodyLines = new ArrayList<>(bodyLines);
        this.puzzle = puzzle;
        this.unlockedFileTitle = unlockedFileTitle;
        this.unlockedFileContent = unlockedFileContent;
    }

    @Override
    public String toString() {
        return title;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public VisualStyle getVisualStyle() {
        return visualStyle;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getScreenshotAsset() {
        return screenshotAsset;
    }

    public List<String> getBodyLines() {
        return Collections.unmodifiableList(bodyLines);
    }

    public Puzzle getPuzzle() {
        return puzzle;
    }

    public String getUnlockedFileTitle() {
        return unlockedFileTitle;
    }

    public String getUnlockedFileContent() {
        return unlockedFileContent;
    }
}
