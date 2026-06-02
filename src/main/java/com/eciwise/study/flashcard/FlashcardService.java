package com.eciwise.study.flashcard;

import com.eciwise.study.exception.ResourceNotFoundException;
import com.eciwise.study.flashcard.dto.FlashcardRequest;
import com.eciwise.study.flashcard.dto.FlashcardResponse;
import com.eciwise.study.user.AppUser;
import com.eciwise.study.user.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final CollectionService collectionService;
    private final CurrentUserService currentUserService;
    private final FlashcardMapper mapper;

    public FlashcardService(FlashcardRepository flashcardRepository,
                            CollectionService collectionService,
                            CurrentUserService currentUserService,
                            FlashcardMapper mapper) {
        this.flashcardRepository = flashcardRepository;
        this.collectionService = collectionService;
        this.currentUserService = currentUserService;
        this.mapper = mapper;
    }

    @Transactional
    public FlashcardResponse create(Long collectionId, FlashcardRequest request) {
        AppUser user = currentUserService.getOrCreate();
        FlashcardCollection collection = collectionService.findByIdOrThrow(collectionId);
        collectionService.ensureCanModify(collection, user);

        Flashcard flashcard = Flashcard.builder()
                .collection(collection)
                .title(request.title())
                .description(request.description())
                .question(request.question())
                .answer(request.answer())
                .build();

        return mapper.toResponse(flashcardRepository.save(flashcard));
    }

    @Transactional
    public List<FlashcardResponse> listByCollection(Long collectionId) {
        AppUser user = currentUserService.getOrCreate();
        FlashcardCollection collection = collectionService.findByIdOrThrow(collectionId);
        collectionService.ensureCanRead(collection, user);
        return flashcardRepository.findByCollection_Id(collectionId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public FlashcardResponse get(Long id) {
        AppUser user = currentUserService.getOrCreate();
        Flashcard flashcard = findByIdOrThrow(id);
        collectionService.ensureCanRead(flashcard.getCollection(), user);
        return mapper.toResponse(flashcard);
    }

    @Transactional
    public FlashcardResponse update(Long id, FlashcardRequest request) {
        AppUser user = currentUserService.getOrCreate();
        Flashcard flashcard = findByIdOrThrow(id);
        collectionService.ensureCanModify(flashcard.getCollection(), user);

        flashcard.setTitle(request.title());
        flashcard.setDescription(request.description());
        flashcard.setQuestion(request.question());
        flashcard.setAnswer(request.answer());
        return mapper.toResponse(flashcardRepository.save(flashcard));
    }

    @Transactional
    public void delete(Long id) {
        AppUser user = currentUserService.getOrCreate();
        Flashcard flashcard = findByIdOrThrow(id);
        collectionService.ensureCanModify(flashcard.getCollection(), user);
        flashcardRepository.delete(flashcard);
    }

    Flashcard findByIdOrThrow(Long id) {
        return flashcardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flash card no encontrada: " + id));
    }
}
