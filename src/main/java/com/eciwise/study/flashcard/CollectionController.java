package com.eciwise.study.flashcard;

import com.eciwise.study.flashcard.dto.CollectionRequest;
import com.eciwise.study.flashcard.dto.CollectionResponse;
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
@RequestMapping("/api/collections")
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping
    public List<CollectionResponse> findAll() {
        return collectionService.listVisible();
    }

    @GetMapping("/{id}")
    public CollectionResponse findById(@PathVariable Long id) {
        return collectionService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CollectionResponse create(@Valid @RequestBody CollectionRequest request) {
        return collectionService.create(request);
    }

    @PutMapping("/{id}")
    public CollectionResponse update(@PathVariable Long id, @Valid @RequestBody CollectionRequest request) {
        return collectionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        collectionService.delete(id);
    }
}
