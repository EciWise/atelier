package com.eciwise.study.flashcard.dto;

/** Cuerpo de {@code PUT /api/collections/{id}/favorite}: fijar o desfijar. */
public record FavoriteRequest(boolean favorite) {
}
