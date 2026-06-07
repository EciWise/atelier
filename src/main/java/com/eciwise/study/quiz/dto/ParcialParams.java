package com.eciwise.study.quiz.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Parametros con los que el modo Parcial decide cuantas y cuales preguntas.
 * preparedness: 1 (nada preparado) .. 5 (muy preparado). targetGrade en escala 0..5.
 */
public record ParcialParams(
        @NotNull @Min(0) Integer daysUntilExam,
        @NotNull @Min(1) @Max(5) Integer preparedness,
        @NotNull @DecimalMin("0.0") @DecimalMax("5.0") BigDecimal targetGrade
) {
}
