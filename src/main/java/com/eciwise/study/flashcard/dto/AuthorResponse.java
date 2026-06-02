package com.eciwise.study.flashcard.dto;

public record AuthorResponse(
        String externalId,
        String email,
        String firstName,
        String lastName,
        String role
) {
}
