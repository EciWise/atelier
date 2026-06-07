package com.eciwise.study.quiz.dto;

import jakarta.validation.constraints.NotBlank;

public record OptionRequest(
        @NotBlank String text,
        boolean correct
) {
}
