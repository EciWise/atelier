package com.eciwise.study.flashcard.dto;

import jakarta.validation.constraints.NotBlank;

public record FlashcardRequest(
        @NotBlank String title,
        String description,
        @NotBlank String question,
        @NotBlank String answer
) {
}
