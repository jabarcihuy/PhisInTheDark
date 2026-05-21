package com.phisinthedark.database;

import com.phisinthedark.puzzle.Puzzle;
import java.util.List;

public class DatabasePuzzle extends Puzzle {
    public DatabasePuzzle(String id, String title, String concept, String codeBlock, 
                          String instruction, String answer, String tutorialAnswer, 
                          String solvedMessage, String rewardItem, List<String> hints) {
        super(id, title, concept, codeBlock, instruction, answer, tutorialAnswer, 
              solvedMessage, rewardItem, hints);
    }

    @Override
    public boolean checkAnswer(String input) {
        return normalize(getAnswer()).equals(normalize(input));
    }
}
