package com.eciwise.study.quiz;

import com.eciwise.study.quiz.dto.QuestionAnswerStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {

    int countBySession_Id(Long sessionId);

    boolean existsBySession_IdAndQuestion_Id(Long sessionId, Long questionId);

    /** Agregados por pregunta: total respondidas y cuantas correctas (sin identidades). */
    @Query("select new com.eciwise.study.quiz.dto.QuestionAnswerStats("
            + "count(a), sum(case when a.correct = true then 1L else 0L end)) "
            + "from QuizAnswer a where a.question.id = :questionId")
    QuestionAnswerStats statsByQuestion(@Param("questionId") Long questionId);

    /** Ids de preguntas (subject+corte) ya respondidas por el usuario en cualquier sesion. */
    @Query("select distinct a.question.id from QuizAnswer a "
            + "where a.session.user.id = :userId "
            + "and a.question.subject.id = :subjectId and a.question.corte = :corte")
    List<Long> answeredQuestionIds(@Param("userId") Long userId,
                                   @Param("subjectId") Long subjectId,
                                   @Param("corte") Integer corte);

    /** Ids de preguntas (subject+corte) que el usuario ha fallado alguna vez. */
    @Query("select distinct a.question.id from QuizAnswer a "
            + "where a.session.user.id = :userId and a.correct = false "
            + "and a.question.subject.id = :subjectId and a.question.corte = :corte")
    List<Long> incorrectQuestionIds(@Param("userId") Long userId,
                                    @Param("subjectId") Long subjectId,
                                    @Param("corte") Integer corte);
}
