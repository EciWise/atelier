package com.eciwise.study.flashcard.dto;

import com.eciwise.study.flashcard.ReviewGrade;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(
        @NotNull ReviewGrade grade
) {
}
