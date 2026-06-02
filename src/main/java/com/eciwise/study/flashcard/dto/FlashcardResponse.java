package com.eciwise.study.flashcard.dto;

import java.time.Instant;

public record FlashcardResponse(
        Long id,
        Long collectionId,
        String title,
        String description,
        String question,
        String answer,
        Instant createdAt,
        Instant updatedAt
) {
}
