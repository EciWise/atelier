package com.eciwise.study.flashcard;

/**
 * Estado de aprendizaje de una flash card para un usuario concreto.
 * Una tarjeta nueva arranca en {@link #EN_APRENDIZAJE} y progresa segun las
 * calificaciones del usuario (ver {@link SpacedRepetitionScheduler}).
 */
public enum ReviewState {
    EN_APRENDIZAJE,
    REPETIR,
    ACEPTABLE,
    APRENDIDO
}
