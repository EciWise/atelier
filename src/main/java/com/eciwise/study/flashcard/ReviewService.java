package com.eciwise.study.flashcard;

import com.eciwise.study.flashcard.dto.ReviewResponse;
import com.eciwise.study.flashcard.dto.ReviewSummaryResponse;
import com.eciwise.study.flashcard.dto.StudyCardResponse;
import com.eciwise.study.user.AppUser;
import com.eciwise.study.user.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Repeticion espaciada de flash cards: registra la calificacion del usuario,
 * reprograma la tarjeta (ver {@link SpacedRepetitionScheduler}) y expone la cola
 * de estudio y el resumen de progreso. El progreso es independiente por usuario.
 */
@Service
public class ReviewService {

    private final FlashcardReviewRepository reviewRepository;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardService flashcardService;
    private final CollectionService collectionService;
    private final CurrentUserService currentUserService;
    private final UsageService usageService;
    private final SpacedRepetitionScheduler scheduler;
    private final FlashcardMapper mapper;

    public ReviewService(FlashcardReviewRepository reviewRepository,
                         FlashcardRepository flashcardRepository,
                         FlashcardService flashcardService,
                         CollectionService collectionService,
                         CurrentUserService currentUserService,
                         UsageService usageService,
                         SpacedRepetitionScheduler scheduler,
                         FlashcardMapper mapper) {
        this.reviewRepository = reviewRepository;
        this.flashcardRepository = flashcardRepository;
        this.flashcardService = flashcardService;
        this.collectionService = collectionService;
        this.currentUserService = currentUserService;
        this.usageService = usageService;
        this.scheduler = scheduler;
        this.mapper = mapper;
    }

    /**
     * Registra la calificacion del usuario sobre una flash card, actualiza su
     * agenda de repeticion espaciada y guarda tambien el historial de uso.
     */
    @Transactional
    public ReviewResponse review(Long flashcardId, ReviewGrade grade) {
        AppUser user = currentUserService.getOrCreate();
        Flashcard flashcard = flashcardService.findByIdOrThrow(flashcardId);
        collectionService.ensureCanRead(flashcard.getCollection(), user);

        Instant now = Instant.now();
        FlashcardReview review = reviewRepository
                .findByUser_IdAndFlashcard_Id(user.getId(), flashcardId)
                .orElseGet(() -> FlashcardReview.builder()
                        .user(user)
                        .flashcard(flashcard)
                        .dueAt(now)
                        .build());

        scheduler.apply(review, grade, now);
        FlashcardReview saved = reviewRepository.save(review);

        // Conserva el historial de uso (base para gamificacion).
        usageService.recordUsage(flashcardId);

        return mapper.toResponse(saved);
    }

    /**
     * Cola de estudio de una coleccion para el usuario actual: tarjetas nunca
     * vistas o cuyo proximo repaso ya vencio.
     */
    @Transactional
    public List<StudyCardResponse> studyQueue(Long collectionId) {
        AppUser user = currentUserService.getOrCreate();
        FlashcardCollection collection = collectionService.findByIdOrThrow(collectionId);
        collectionService.ensureCanRead(collection, user);

        Instant now = Instant.now();
        Map<Long, FlashcardReview> reviewsByCard = reviewRepository
                .findByUser_IdAndFlashcard_Collection_Id(user.getId(), collectionId).stream()
                .collect(Collectors.toMap(r -> r.getFlashcard().getId(), Function.identity()));

        return flashcardRepository.findByCollection_Id(collectionId).stream()
                .filter(card -> {
                    FlashcardReview review = reviewsByCard.get(card.getId());
                    return review == null || !review.getDueAt().isAfter(now);
                })
                .map(card -> mapper.toStudyCard(card, reviewsByCard.get(card.getId())))
                .toList();
    }

    /**
     * Resumen del progreso del usuario actual: conteo por estado y vencidas.
     */
    @Transactional
    public ReviewSummaryResponse mySummary() {
        AppUser user = currentUserService.getOrCreate();
        List<FlashcardReview> reviews = reviewRepository.findByUser_Id(user.getId());
        Instant now = Instant.now();

        long enAprendizaje = countByState(reviews, ReviewState.EN_APRENDIZAJE);
        long repetir = countByState(reviews, ReviewState.REPETIR);
        long aceptable = countByState(reviews, ReviewState.ACEPTABLE);
        long aprendido = countByState(reviews, ReviewState.APRENDIDO);
        long vencidas = reviews.stream().filter(r -> !r.getDueAt().isAfter(now)).count();

        return new ReviewSummaryResponse(
                reviews.size(), enAprendizaje, repetir, aceptable, aprendido, vencidas);
    }

    private long countByState(List<FlashcardReview> reviews, ReviewState state) {
        return reviews.stream().filter(r -> r.getState() == state).count();
    }
}
