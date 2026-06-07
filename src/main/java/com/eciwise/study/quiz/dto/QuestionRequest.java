package com.eciwise.study.quiz.dto;

import com.eciwise.study.quiz.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Alta/edicion de una pregunta. La validacion fina por tipo (opciones de CLOSED,
 * correctAnswer de OPEN/TRUE_FALSE) se hace en QuestionService.
 */
public record QuestionRequest(
        @NotNull Long subjectId,
        @NotNull @Min(1) @Max(3) Integer corte,
        @NotNull QuestionType type,
        @NotBlank String statement,
        String explanation,
        String correctAnswer,
        boolean availableForSurvival,
        @Min(5) @Max(300) Integer timeLimitSeconds,
        @Valid List<OptionRequest> options
) {
}
