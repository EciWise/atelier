package com.eciwise.study.flashcard.dto;

import com.eciwise.study.flashcard.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CollectionRequest(
        @NotBlank String name,
        @NotNull Visibility visibility
) {
}
