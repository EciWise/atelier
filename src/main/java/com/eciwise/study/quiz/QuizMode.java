package com.eciwise.study.quiz;

/**
 * Modos de juego:
 * PARCIAL: seleccion del banco por materia+corte segun la preparacion del estudiante.
 * REPASO: las preguntas de una coleccion creada por admin/tutor.
 * SUPERVIVENCIA: todas las preguntas marcadas como disponibles, con vidas y ranking publico.
 */
public enum QuizMode {
    PARCIAL,
    REPASO,
    SUPERVIVENCIA
}
