package com.eciwise.study.quiz.dto;

/** Proyeccion de agregados de respuestas por pregunta (total y aciertos). */
public record QuestionAnswerStats(
        Long total,
        Long correct
) {
}
