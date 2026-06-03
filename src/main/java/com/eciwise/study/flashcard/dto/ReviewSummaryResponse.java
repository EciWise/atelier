package com.eciwise.study.flashcard.dto;

/**
 * Resumen del progreso del usuario: total de tarjetas con review y conteo por
 * estado, mas cuantas estan vencidas (listas para repasar).
 */
public record ReviewSummaryResponse(
        long total,
        long enAprendizaje,
        long repetir,
        long aceptable,
        long aprendido,
        long vencidas
) {
}
