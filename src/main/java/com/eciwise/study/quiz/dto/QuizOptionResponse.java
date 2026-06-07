package com.eciwise.study.quiz.dto;

/** Opcion tal como la ve el estudiante mientras juega: sin revelar cual es correcta. */
public record QuizOptionResponse(
        Long id,
        String text,
        int position
) {
}
