package com.eciwise.study.quiz.dto;

/** Proyeccion intermedia del ranking (mejor puntaje por usuario). */
public record LeaderboardScore(
        Long userId,
        String firstName,
        String lastName,
        Integer bestScore
) {
}
