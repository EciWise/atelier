package com.eciwise.study.quiz;

/**
 * Tipos de pregunta soportados por el banco.
 * CLOSED: opcion multiple (la verdad vive en question_options).
 * OPEN: respuesta abierta (se compara de forma normalizada contra correct_answer).
 * TRUE_FALSE: verdadero/falso (correct_answer almacena "true"/"false").
 */
public enum QuestionType {
    CLOSED,
    OPEN,
    TRUE_FALSE
}
