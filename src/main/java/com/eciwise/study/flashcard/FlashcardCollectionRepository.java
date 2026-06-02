package com.eciwise.study.flashcard;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlashcardCollectionRepository extends JpaRepository<FlashcardCollection, Long> {

    List<FlashcardCollection> findByAuthor_Id(Long authorId);

    List<FlashcardCollection> findByVisibility(Visibility visibility);

    /**
     * Colecciones visibles para un usuario: las publicas o las que le pertenecen.
     */
    @Query("select c from FlashcardCollection c "
            + "where c.visibility = com.eciwise.study.flashcard.Visibility.PUBLIC "
            + "or c.author.id = :userId")
    List<FlashcardCollection> findVisibleTo(@Param("userId") Long userId);
}
