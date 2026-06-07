package com.eciwise.study.quiz.dto;

/** Vista de opcion para admin/tutor (incluye si es correcta). */
public record OptionResponse(
        Long id,
        String text,
        boolean correct,
        int position
) {
}
