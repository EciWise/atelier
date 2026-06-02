package com.eciwise.study.flashcard.dto;

import com.eciwise.study.flashcard.Visibility;

import java.time.Instant;

public record CollectionResponse(
        Long id,
        String name,
        Visibility visibility,
        AuthorResponse author,
        int flashcardCount,
        Instant createdAt,
        Instant updatedAt
) {
}
