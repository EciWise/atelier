package com.eciwise.study.flashcard;

import com.eciwise.study.flashcard.dto.FlashcardRequest;
import com.eciwise.study.flashcard.dto.FlashcardResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FlashcardController {

    private final FlashcardService flashcardService;

    public FlashcardController(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
    }

    @GetMapping("/collections/{collectionId}/flashcards")
    public List<FlashcardResponse> findByCollection(@PathVariable Long collectionId) {
        return flashcardService.listByCollection(collectionId);
    }

    @PostMapping("/collections/{collectionId}/flashcards")
    @ResponseStatus(HttpStatus.CREATED)
    public FlashcardResponse create(@PathVariable Long collectionId,
                                    @Valid @RequestBody FlashcardRequest request) {
        return flashcardService.create(collectionId, request);
    }

    @GetMapping("/flashcards/{id}")
    public FlashcardResponse findById(@PathVariable Long id) {
        return flashcardService.get(id);
    }

    @PutMapping("/flashcards/{id}")
    public FlashcardResponse update(@PathVariable Long id, @Valid @RequestBody FlashcardRequest request) {
        return flashcardService.update(id, request);
    }

    @DeleteMapping("/flashcards/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        flashcardService.delete(id);
    }
}
