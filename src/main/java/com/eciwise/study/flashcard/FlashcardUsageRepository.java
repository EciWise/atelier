package com.eciwise.study.flashcard;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlashcardUsageRepository extends JpaRepository<FlashcardUsage, Long> {

    List<FlashcardUsage> findByUser_IdOrderByUsedAtDesc(Long userId);

    long countByUser_Id(Long userId);
}
