package com.eciwise.study.quiz.dto;

import com.eciwise.study.quiz.SessionStatus;

/**
 * Resultado de una respuesta. Aqui SI se revela la verdad (la pregunta ya fue contestada):
 * la opcion correcta o el texto correcto, mas la explicacion.
 */
public record AnswerResultResponse(
        boolean correct,
        Long correctOptionId,
        String correctAnswer,
        String explanation,
        int pointsAwarded,
        Integer livesRemaining,
        SessionStatus status,
        int score,
        int correctCount,
        int incorrectCount,
        int totalAnswered
) {
}
