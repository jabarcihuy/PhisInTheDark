package com.phisinthedark.puzzle;

public interface Solvable {
    boolean checkAnswer(String answer);

    String getHint(int attempts);
}
