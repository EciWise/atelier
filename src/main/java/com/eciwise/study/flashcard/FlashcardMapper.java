package com.eciwise.study.flashcard;

import com.eciwise.study.flashcard.dto.AuthorResponse;
import com.eciwise.study.flashcard.dto.CollectionResponse;
import com.eciwise.study.flashcard.dto.FlashcardResponse;
import com.eciwise.study.flashcard.dto.ReviewResponse;
import com.eciwise.study.flashcard.dto.StudyCardResponse;
import com.eciwise.study.flashcard.dto.UsageResponse;
import com.eciwise.study.user.AppUser;
import org.springframework.stereotype.Component;

@Component
public class FlashcardMapper {

    public CollectionResponse toResponse(FlashcardCollection collection) {
        return new CollectionResponse(
                collection.getId(),
                collection.getName(),
                collection.getVisibility(),
                toAuthorResponse(collection.getAuthor()),
                collection.getFlashcards() == null ? 0 : collection.getFlashcards().size(),
                collection.getCreatedAt(),
                collection.getUpdatedAt()
        );
    }

    public AuthorResponse toAuthorResponse(AppUser author) {
        return new AuthorResponse(
                author.getExternalId(),
                author.getEmail(),
                author.getFirstName(),
                author.getLastName(),
                author.getRole()
        );
    }

    public FlashcardResponse toResponse(Flashcard flashcard) {
        return new FlashcardResponse(
                flashcard.getId(),
                flashcard.getCollection().getId(),
                flashcard.getTitle(),
                flashcard.getDescription(),
                flashcard.getQuestion(),
                flashcard.getAnswer(),
                flashcard.getCreatedAt(),
                flashcard.getUpdatedAt()
        );
    }

    public UsageResponse toResponse(FlashcardUsage usage) {
        return new UsageResponse(
                usage.getFlashcard().getId(),
                usage.getFlashcard().getTitle(),
                usage.getUsedAt()
        );
    }

    public ReviewResponse toResponse(FlashcardReview review) {
        return new ReviewResponse(
                review.getFlashcard().getId(),
                review.getState(),
                review.getRepetitions(),
                review.getIntervalDays(),
                review.getEaseFactor(),
                review.getLapses(),
                review.getDueAt(),
                review.getLastReviewedAt()
        );
    }

    public StudyCardResponse toStudyCard(Flashcard flashcard, FlashcardReview review) {
        return new StudyCardResponse(
                toResponse(flashcard),
                review == null ? null : review.getState(),
                review == null ? null : review.getDueAt()
        );
    }
}
