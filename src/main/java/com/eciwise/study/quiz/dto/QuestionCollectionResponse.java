package com.eciwise.study.quiz.dto;

import java.time.Instant;

public record QuestionCollectionResponse(
        Long id,
        String name,
        String description,
        Long subjectId,
        String subjectName,
        int questionCount,
        Instant createdAt,
        Instant updatedAt
) {
}
