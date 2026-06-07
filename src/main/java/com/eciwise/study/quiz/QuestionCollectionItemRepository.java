package com.eciwise.study.quiz;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionCollectionItemRepository extends JpaRepository<QuestionCollectionItem, Long> {

    boolean existsByCollection_IdAndQuestion_Id(Long collectionId, Long questionId);
}
