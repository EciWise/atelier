package com.eciwise.study.flashcard;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SpacedRepetitionSchedulerTest {

    private final SpacedRepetitionScheduler scheduler = new SpacedRepetitionScheduler();
    private final Instant now = Instant.parse("2026-06-02T10:00:00Z");

    private FlashcardReview nuevaTarjeta() {
        return FlashcardReview.builder().build();
    }

    @Test
    void tarjetaNuevaArrancaEnAprendizaje() {
        FlashcardReview review = nuevaTarjeta();
        assertThat(review.getState()).isEqualTo(ReviewState.EN_APRENDIZAJE);
        assertThat(review.getEaseFactor()).isEqualTo(2.5);
        assertThat(review.getRepetitions()).isZero();
    }

    @Test
    void aceptablePrimeroProgramaUnDiaYSigueEnAprendizaje() {
        FlashcardReview review = nuevaTarjeta();
        scheduler.apply(review, ReviewGrade.ACEPTABLE, now);

        assertThat(review.getIntervalDays()).isEqualTo(1);
        assertThat(review.getRepetitions()).isEqualTo(1);
        assertThat(review.getState()).isEqualTo(ReviewState.EN_APRENDIZAJE);
        assertThat(review.getDueAt()).isEqualTo(now.plus(Duration.ofDays(1)));
    }

    @Test
    void aceptableSegundoProgramaTresDiasYPasaAAceptable() {
        FlashcardReview review = nuevaTarjeta();
        scheduler.apply(review, ReviewGrade.ACEPTABLE, now);
        scheduler.apply(review, ReviewGrade.ACEPTABLE, now);

        assertThat(review.getIntervalDays()).isEqualTo(3);
        assertThat(review.getRepetitions()).isEqualTo(2);
        assertThat(review.getState()).isEqualTo(ReviewState.ACEPTABLE);
    }

    @Test
    void repetirReiniciaYDejaEstadoRepetir() {
        FlashcardReview review = nuevaTarjeta();
        scheduler.apply(review, ReviewGrade.ACEPTABLE, now);
        scheduler.apply(review, ReviewGrade.ACEPTABLE, now);

        scheduler.apply(review, ReviewGrade.REPETIR, now);

        assertThat(review.getState()).isEqualTo(ReviewState.REPETIR);
        assertThat(review.getRepetitions()).isZero();
        assertThat(review.getIntervalDays()).isZero();
        assertThat(review.getLapses()).isEqualTo(1);
        assertThat(review.getEaseFactor()).isEqualTo(2.5 - 0.20);
        assertThat(review.getDueAt()).isEqualTo(now.plus(Duration.ofMinutes(10)));
    }

    @Test
    void easeFactorNuncaBajaDelMinimo() {
        FlashcardReview review = nuevaTarjeta();
        for (int i = 0; i < 20; i++) {
            scheduler.apply(review, ReviewGrade.REPETIR, now);
        }
        assertThat(review.getEaseFactor()).isGreaterThanOrEqualTo(1.3);
    }

    @Test
    void repasosAprendidoLleganAEstadoAprendido() {
        FlashcardReview review = nuevaTarjeta();
        for (int i = 0; i < 6; i++) {
            scheduler.apply(review, ReviewGrade.APRENDIDO, now);
        }
        assertThat(review.getIntervalDays()).isGreaterThanOrEqualTo(21);
        assertThat(review.getState()).isEqualTo(ReviewState.APRENDIDO);
    }
}
