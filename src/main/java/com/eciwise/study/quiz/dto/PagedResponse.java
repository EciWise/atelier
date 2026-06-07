package com.eciwise.study.quiz.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/** Envoltura de paginacion estable para la API (evita serializar el Page de Spring). */
public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PagedResponse<T> of(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
