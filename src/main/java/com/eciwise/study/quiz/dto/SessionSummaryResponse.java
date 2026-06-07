package com.eciwise.study.quiz.dto;

import com.eciwise.study.quiz.QuizMode;
import com.eciwise.study.quiz.SessionStatus;

import java.time.Instant;

/** Resumen de una sesion para el historial del usuario. */
public record SessionSummaryResponse(
        Long id,
        QuizMode mode,
        SessionStatus status,
        Long subjectId,
        Integer corte,
        Long collectionId,
        int totalQuestions,
        int correctCount,
        int incorrectCount,
        int score,
        double accuracyPercent,
        Integer livesRemaining,
        Instant startedAt,
        Instant finishedAt
) {
}
