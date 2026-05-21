package com.phisinthedark.puzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public abstract class Puzzle implements Solvable {
    private final String id;
    private final String title;
    private final String concept;
    private final String codeBlock;
    private final String instruction;
    private final String tutorialAnswer;
    private final String solvedMessage;
    private final String rewardItem;
    private final List<String> hints;
    private final List<List<String>> acceptedTokenGroups;

    protected Puzzle(String id,
                     String title,
                     String concept,
                     String codeBlock,
                     String instruction,
                     String tutorialAnswer,
                     String solvedMessage,
                     String rewardItem,
                     List<String> hints) {
        this.id = id;
        this.title = title;
        this.concept = concept;
        this.codeBlock = codeBlock;
        this.instruction = instruction;
        this.tutorialAnswer = tutorialAnswer;
        this.solvedMessage = solvedMessage;
        this.rewardItem = rewardItem;
        this.hints = new ArrayList<>(hints);
        this.acceptedTokenGroups = new ArrayList<>();
    }

    protected final void acceptTokens(String... tokens) {
        acceptedTokenGroups.add(Arrays.asList(tokens));
    }

    @Override
    public boolean checkAnswer(String answer) {
        String normalized = normalize(answer);
        if (normalized.isBlank()) {
            return false;
        }

        for (List<String> tokenGroup : acceptedTokenGroups) {
            boolean matched = true;
            for (String token : tokenGroup) {
                if (!normalized.contains(normalize(token))) {
                    matched = false;
                    break;
                }
            }
            if (matched) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getHint(int attempts) {
        if (hints.isEmpty()) {
            return "Coba baca nilai variable dan perintah yang paling jelas.";
        }
        int index = Math.min(Math.max(0, attempts), hints.size() - 1);
        return hints.get(index);
    }

    protected String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
                .replace("\"", " ")
                .replace("'", " ")
                .replaceAll("[^a-z0-9_]+", " ")
                .trim();
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getConcept() {
        return concept;
    }

    public String getCodeBlock() {
        return codeBlock;
    }

    public String getInstruction() {
        return instruction;
    }

    public String getTutorialAnswer() {
        return tutorialAnswer;
    }

    public String getSolvedMessage() {
        return solvedMessage;
    }

    public String getRewardItem() {
        return rewardItem;
    }
}
