package com.eciwise.study.quiz;

import com.eciwise.study.quiz.dto.LeaderboardScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {

    Page<QuizSession> findByUser_IdOrderByStartedAtDesc(Long userId, Pageable pageable);

    /** Tabla general del ranking: mejor puntaje por usuario en Supervivencia completada. */
    @Query(value = "select new com.eciwise.study.quiz.dto.LeaderboardScore(u.id, u.firstName, u.lastName, max(s.score)) "
            + "from QuizSession s join s.user u "
            + "where s.mode = com.eciwise.study.quiz.QuizMode.SUPERVIVENCIA "
            + "and s.status = com.eciwise.study.quiz.SessionStatus.COMPLETED "
            + "group by u.id, u.firstName, u.lastName "
            + "order by max(s.score) desc",
            countQuery = "select count(distinct s.user.id) from QuizSession s "
            + "where s.mode = com.eciwise.study.quiz.QuizMode.SUPERVIVENCIA "
            + "and s.status = com.eciwise.study.quiz.SessionStatus.COMPLETED")
    Page<LeaderboardScore> leaderboard(Pageable pageable);

    @Query("select max(s.score) from QuizSession s where s.user.id = :userId "
            + "and s.mode = com.eciwise.study.quiz.QuizMode.SUPERVIVENCIA "
            + "and s.status = com.eciwise.study.quiz.SessionStatus.COMPLETED")
    Integer findBestSurvivalScore(@Param("userId") Long userId);

    /** Cuantos usuarios tienen un mejor puntaje que el dado (para calcular el puesto). */
    @Query(value = "select count(*) from ("
            + "select max(score) ms from quiz_sessions "
            + "where mode = 'SUPERVIVENCIA' and status = 'COMPLETED' "
            + "group by user_id having max(score) > :score) t", nativeQuery = true)
    long countUsersWithBetterScore(@Param("score") int score);
}
