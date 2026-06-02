package com.eciwise.study.flashcard;

import com.eciwise.study.flashcard.dto.UsageResponse;
import com.eciwise.study.flashcard.dto.UsageSummaryResponse;
import com.eciwise.study.user.AppUser;
import com.eciwise.study.user.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsageService {

    private final FlashcardUsageRepository usageRepository;
    private final FlashcardService flashcardService;
    private final CollectionService collectionService;
    private final CurrentUserService currentUserService;
    private final FlashcardMapper mapper;

    public UsageService(FlashcardUsageRepository usageRepository,
                        FlashcardService flashcardService,
                        CollectionService collectionService,
                        CurrentUserService currentUserService,
                        FlashcardMapper mapper) {
        this.usageRepository = usageRepository;
        this.flashcardService = flashcardService;
        this.collectionService = collectionService;
        this.currentUserService = currentUserService;
        this.mapper = mapper;
    }

    /**
     * Registra el uso de una flash card por el usuario actual.
     */
    @Transactional
    public UsageResponse recordUsage(Long flashcardId) {
        AppUser user = currentUserService.getOrCreate();
        Flashcard flashcard = flashcardService.findByIdOrThrow(flashcardId);
        collectionService.ensureCanRead(flashcard.getCollection(), user);

        FlashcardUsage usage = FlashcardUsage.builder()
                .user(user)
                .flashcard(flashcard)
                .build();
        return mapper.toResponse(usageRepository.save(usage));
    }

    /**
     * Resumen e historial de uso del usuario actual (base para gamificacion).
     */
    @Transactional
    public UsageSummaryResponse mySummary() {
        AppUser user = currentUserService.getOrCreate();
        List<UsageResponse> history = usageRepository.findByUser_IdOrderByUsedAtDesc(user.getId()).stream()
                .map(mapper::toResponse)
                .toList();
        long total = usageRepository.countByUser_Id(user.getId());
        return new UsageSummaryResponse(total, history);
    }
}
