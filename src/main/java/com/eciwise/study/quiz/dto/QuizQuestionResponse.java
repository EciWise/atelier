package com.eciwise.study.quiz.dto;

import com.eciwise.study.quiz.QuestionType;

import java.util.List;

/**
 * Pregunta servida durante un quiz. NUNCA incluye la respuesta correcta ni la
 * marca de opcion correcta (eso solo se revela al responder).
 */
public record QuizQuestionResponse(
        Long id,
        QuestionType type,
        String statement,
        int timeLimitSeconds,
        List<QuizOptionResponse> options
) {
}
