package com.eciwise.study.quiz.dto;

/** Estadisticas agregadas de una pregunta para admin: cuantos, no quienes. */
public record QuestionStatsResponse(
        Long questionId,
        long timesAnswered,
        long correct,
        long incorrect,
        double correctRate
) {
}
