package com.eciwise.study.quiz.dto;

/**
 * Ranking publico de Supervivencia: la tabla general paginada mas el puesto y mejor
 * puntaje del usuario consultor (null si aun no ha completado una partida).
 */
public record LeaderboardResponse(
        PagedResponse<LeaderboardEntryResponse> table,
        Long myRank,
        Integer myBestScore
) {
}
