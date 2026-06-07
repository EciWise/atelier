package com.eciwise.study.quiz;

import org.springframework.stereotype.Component;

/**
 * Puntaje estilo Kahoot: una respuesta correcta vale BASE_POINTS con una penalizacion
 * proporcional al tiempo empleado (mas rapido, mas puntos). El tiempo se clampa a
 * [0, limite] antes de calcular para evitar manipulacion del cliente.
 */
@Component
public class QuizScorer {

    static final int BASE_POINTS = 1000;
    static final double SPEED_WEIGHT = 0.5;

    public int points(boolean correct, Integer timeTakenMs, int timeLimitSeconds) {
        if (!correct) {
            return 0;
        }
        long limitMs = Math.max(1L, (long) timeLimitSeconds * 1000L);
        long taken = timeTakenMs == null ? 0L : Math.max(0L, Math.min(timeTakenMs.longValue(), limitMs));
        double fraction = (double) taken / limitMs;
        return (int) Math.round(BASE_POINTS * (1.0 - SPEED_WEIGHT * fraction));
    }
}
