package com.eciwise.study.quiz.dto;

import com.eciwise.study.quiz.QuizMode;
import com.eciwise.study.quiz.SessionStatus;

import java.time.Instant;
import java.util.List;

/** Sesion recien iniciada con las preguntas a jugar (sin respuestas correctas). */
public record SessionResponse(
        Long id,
        QuizMode mode,
        SessionStatus status,
        int totalQuestions,
        Integer livesRemaining,
        Long subjectId,
        Integer corte,
        Long collectionId,
        Instant startedAt,
        List<QuizQuestionResponse> questions
) {
}
