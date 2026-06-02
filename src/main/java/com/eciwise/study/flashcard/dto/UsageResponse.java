package com.eciwise.study.flashcard.dto;

import java.time.Instant;

public record UsageResponse(
        Long flashcardId,
        String flashcardTitle,
        Instant usedAt
) {
}
