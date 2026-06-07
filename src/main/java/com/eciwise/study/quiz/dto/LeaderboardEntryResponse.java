package com.eciwise.study.quiz.dto;

/** Fila del ranking publico de Supervivencia con su puesto. No expone datos sensibles. */
public record LeaderboardEntryResponse(
        long rank,
        Long userId,
        String name,
        int bestScore
) {
}
