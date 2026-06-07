package com.eciwise.study.quiz;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findBySubject_IdAndCorte(Long subjectId, Integer corte);

    List<Question> findBySubject_Id(Long subjectId);

    List<Question> findByAvailableForSurvivalTrue();

    boolean existsBySubject_Id(Long subjectId);
}
