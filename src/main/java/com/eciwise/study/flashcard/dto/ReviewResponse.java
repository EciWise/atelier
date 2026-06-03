package com.eciwise.study.flashcard.dto;

import com.eciwise.study.flashcard.ReviewState;

import java.time.Instant;

public record ReviewResponse(
        Long flashcardId,
        ReviewState state,
        int repetitions,
        int intervalDays,
        double easeFactor,
        int lapses,
        Instant dueAt,
        Instant lastReviewedAt
) {
}
