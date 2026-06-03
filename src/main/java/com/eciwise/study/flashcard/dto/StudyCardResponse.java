package com.eciwise.study.flashcard.dto;

import com.eciwise.study.flashcard.ReviewState;

import java.time.Instant;

/**
 * Tarjeta pendiente de estudiar: incluye la flash card y el estado/agenda
 * actuales del usuario (null cuando aun no la ha visto nunca).
 */
public record StudyCardResponse(
        FlashcardResponse card,
        ReviewState state,
        Instant dueAt
) {
}
