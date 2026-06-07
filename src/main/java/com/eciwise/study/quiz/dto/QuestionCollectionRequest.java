package com.eciwise.study.quiz.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record QuestionCollectionRequest(
        @NotBlank String name,
        String description,
        Long subjectId,
        List<Long> questionIds
) {
}
