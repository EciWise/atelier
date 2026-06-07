package com.eciwise.study.subject.dto;

import java.time.Instant;

public record SubjectResponse(
        Long id,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
