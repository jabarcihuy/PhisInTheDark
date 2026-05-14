package com.nulltrace.puzzle;

public interface Solvable {
    boolean checkAnswer(String answer);

    String getHint(int attempts);
}
