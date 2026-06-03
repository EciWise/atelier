package com.eciwise.study.flashcard;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Algoritmo de repeticion espaciada (SM-2 adaptado a tres calificaciones:
 * REPETIR / ACEPTABLE / APRENDIDO).
 *
 * <p>Es una clase sin estado: {@link #apply(FlashcardReview, ReviewGrade, Instant)}
 * muta el {@link FlashcardReview} recibido con el nuevo intervalo, factor de
 * facilidad, estado y fecha del proximo repaso. Las constantes son ajustables.
 */
@Component
public class SpacedRepetitionScheduler {

    /** Intervalo (en dias) a partir del cual una tarjeta se considera APRENDIDA. */
    static final int LEARNED_THRESHOLD_DAYS = 21;
    /** Factor de facilidad minimo permitido. */
    static final double MIN_EASE = 1.3;
    /** Factor de facilidad inicial. */
    static final double DEFAULT_EASE = 2.5;
    /** Penalizacion de facilidad al pulsar REPETIR. */
    static final double REPETIR_EASE_PENALTY = 0.20;
    /** Bonificacion de facilidad al pulsar APRENDIDO. */
    static final double APRENDIDO_EASE_BONUS = 0.15;
    /** Multiplicador extra de intervalo para la calificacion APRENDIDO. */
    static final double APRENDIDO_INTERVAL_BONUS = 1.3;
    /** Espera para volver a ver una tarjeta marcada como REPETIR. */
    static final Duration REPETIR_DELAY = Duration.ofMinutes(10);

    /**
     * Aplica una calificacion al review y reprograma su proximo repaso.
     */
    public void apply(FlashcardReview review, ReviewGrade grade, Instant now) {
        switch (grade) {
            case REPETIR -> applyRepetir(review, now);
            case ACEPTABLE -> applyAceptable(review, now);
            case APRENDIDO -> applyAprendido(review, now);
        }
        review.setLastReviewedAt(now);
    }

    private void applyRepetir(FlashcardReview review, Instant now) {
        review.setLapses(review.getLapses() + 1);
        review.setRepetitions(0);
        review.setIntervalDays(0);
        review.setEaseFactor(Math.max(MIN_EASE, review.getEaseFactor() - REPETIR_EASE_PENALTY));
        review.setState(ReviewState.REPETIR);
        review.setDueAt(now.plus(REPETIR_DELAY));
    }

    private void applyAceptable(FlashcardReview review, Instant now) {
        int interval = switch (review.getRepetitions()) {
            case 0 -> 1;
            case 1 -> 3;
            default -> (int) Math.round(review.getIntervalDays() * review.getEaseFactor());
        };
        review.setRepetitions(review.getRepetitions() + 1);
        review.setIntervalDays(interval);
        review.setDueAt(now.plus(Duration.ofDays(interval)));
        review.setState(deriveState(review, interval));
    }

    private void applyAprendido(FlashcardReview review, Instant now) {
        int interval = review.getRepetitions() == 0
                ? 2
                : (int) Math.round(review.getIntervalDays() * review.getEaseFactor() * APRENDIDO_INTERVAL_BONUS);
        review.setRepetitions(review.getRepetitions() + 1);
        review.setEaseFactor(review.getEaseFactor() + APRENDIDO_EASE_BONUS);
        review.setIntervalDays(interval);
        review.setDueAt(now.plus(Duration.ofDays(interval)));
        review.setState(deriveState(review, interval));
    }

    /**
     * Deriva el estado tras una calificacion positiva: APRENDIDO cuando el
     * intervalo supera el umbral, ACEPTABLE cuando ya hay varios repasos, y
     * EN_APRENDIZAJE mientras la tarjeta sigue siendo reciente.
     */
    private ReviewState deriveState(FlashcardReview review, int interval) {
        if (interval >= LEARNED_THRESHOLD_DAYS) {
            return ReviewState.APRENDIDO;
        }
        return review.getRepetitions() >= 2 ? ReviewState.ACEPTABLE : ReviewState.EN_APRENDIZAJE;
    }
}
