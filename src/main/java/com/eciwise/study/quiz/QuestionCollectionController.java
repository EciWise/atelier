package com.eciwise.study.quiz;

import com.eciwise.study.quiz.dto.QuestionCollectionRequest;
import com.eciwise.study.quiz.dto.QuestionCollectionResponse;
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

/** Colecciones de preguntas para el modo Repaso. */
@RestController
@RequestMapping("/api/question-collections")
public class QuestionCollectionController {

    private final QuestionCollectionService collectionService;

    public QuestionCollectionController(QuestionCollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping
    public List<QuestionCollectionResponse> findAll() {
        return collectionService.list();
    }

    @GetMapping("/{id}")
    public QuestionCollectionResponse findById(@PathVariable Long id) {
        return collectionService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionCollectionResponse create(@Valid @RequestBody QuestionCollectionRequest request) {
        return collectionService.create(request);
    }

    @PutMapping("/{id}")
    public QuestionCollectionResponse update(@PathVariable Long id,
                                             @Valid @RequestBody QuestionCollectionRequest request) {
        return collectionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        collectionService.delete(id);
    }
}
