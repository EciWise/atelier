package com.eciwise.study.subject.dto;

import jakarta.validation.constraints.NotBlank;

public record SubjectRequest(
        @NotBlank String name,
        String description
) {
}
