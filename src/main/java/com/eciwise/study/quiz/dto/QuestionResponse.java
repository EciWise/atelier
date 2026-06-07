package com.eciwise.study.quiz.dto;

import com.eciwise.study.quiz.QuestionType;

import java.time.Instant;
import java.util.List;

/** Vista completa de una pregunta para admin/tutor (incluye la respuesta correcta). */
public record QuestionResponse(
        Long id,
        Long subjectId,
        String subjectName,
        Integer corte,
        QuestionType type,
        String statement,
        String explanation,
        String correctAnswer,
        boolean availableForSurvival,
        int timeLimitSeconds,
        List<OptionResponse> options,
        Instant createdAt,
        Instant updatedAt
) {
}
