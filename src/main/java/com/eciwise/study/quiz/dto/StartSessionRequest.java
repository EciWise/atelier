package com.eciwise.study.quiz.dto;

import com.eciwise.study.quiz.QuizMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Inicia una sesion. Segun el modo se requieren distintos campos (validado en QuizService):
 * PARCIAL -> subjectId + corte + parcial; REPASO -> collectionId; SUPERVIVENCIA -> nada extra.
 */
public record StartSessionRequest(
        @NotNull QuizMode mode,
        Long subjectId,
        Integer corte,
        Long collectionId,
        @Valid ParcialParams parcial
) {
}
