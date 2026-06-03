package com.eciwise.study.flashcard;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FlashcardReviewRepository extends JpaRepository<FlashcardReview, Long> {

    Optional<FlashcardReview> findByUser_IdAndFlashcard_Id(Long userId, Long flashcardId);

    List<FlashcardReview> findByUser_Id(Long userId);

    List<FlashcardReview> findByUser_IdAndFlashcard_Collection_Id(Long userId, Long collectionId);

    long countByUser_IdAndState(Long userId, ReviewState state);
}
