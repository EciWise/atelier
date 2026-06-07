package com.eciwise.study.quiz.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Respuesta del jugador. Para CLOSED se usa selectedOptionId; para OPEN/TRUE_FALSE,
 * givenAnswer. timeTakenMs alimenta el bonus de rapidez (se clampa en el scorer).
 */
public record AnswerRequest(
        @NotNull Long questionId,
        Long selectedOptionId,
        String givenAnswer,
        @NotNull @Min(0) Integer timeTakenMs
) {
}
