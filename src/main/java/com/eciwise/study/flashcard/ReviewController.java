package com.eciwise.study.flashcard;

import com.eciwise.study.flashcard.dto.ReviewRequest;
import com.eciwise.study.flashcard.dto.ReviewResponse;
import com.eciwise.study.flashcard.dto.ReviewSummaryResponse;
import com.eciwise.study.flashcard.dto.StudyCardResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/flashcards/{id}/review")
    public ReviewResponse review(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        return reviewService.review(id, request.grade());
    }

    @GetMapping("/collections/{collectionId}/study")
    public List<StudyCardResponse> study(@PathVariable Long collectionId) {
        return reviewService.studyQueue(collectionId);
    }

    @GetMapping("/reviews/me")
    public ReviewSummaryResponse mySummary() {
        return reviewService.mySummary();
    }
}
