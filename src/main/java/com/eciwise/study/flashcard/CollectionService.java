package com.eciwise.study.flashcard;

import com.eciwise.study.exception.ForbiddenOperationException;
import com.eciwise.study.exception.ResourceNotFoundException;
import com.eciwise.study.flashcard.dto.CollectionRequest;
import com.eciwise.study.flashcard.dto.CollectionResponse;
import com.eciwise.study.user.AppUser;
import com.eciwise.study.user.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CollectionService {

    static final String ROLE_ADMIN = "admin";
    static final String ROLE_ESTUDIANTE = "estudiante";

    private final FlashcardCollectionRepository collectionRepository;
    private final CurrentUserService currentUserService;
    private final FlashcardMapper mapper;

    public CollectionService(FlashcardCollectionRepository collectionRepository,
                             CurrentUserService currentUserService,
                             FlashcardMapper mapper) {
        this.collectionRepository = collectionRepository;
        this.currentUserService = currentUserService;
        this.mapper = mapper;
    }

    @Transactional
    public CollectionResponse create(CollectionRequest request) {
        AppUser user = currentUserService.getOrCreate();
        validateVisibility(request.visibility(), user);

        FlashcardCollection collection = FlashcardCollection.builder()
                .name(request.name())
                .author(user)
                .visibility(request.visibility())
                .build();

        return mapper.toResponse(collectionRepository.save(collection));
    }

    @Transactional
    public List<CollectionResponse> listVisible() {
        AppUser user = currentUserService.getOrCreate();
        List<FlashcardCollection> collections = isAdmin(user)
                ? collectionRepository.findAll()
                : collectionRepository.findVisibleTo(user.getId());
        return collections.stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public CollectionResponse get(Long id) {
        AppUser user = currentUserService.getOrCreate();
        FlashcardCollection collection = findByIdOrThrow(id);
        ensureCanRead(collection, user);
        return mapper.toResponse(collection);
    }

    @Transactional
    public CollectionResponse update(Long id, CollectionRequest request) {
        AppUser user = currentUserService.getOrCreate();
        FlashcardCollection collection = findByIdOrThrow(id);
        ensureCanModify(collection, user);
        validateVisibility(request.visibility(), user);

        collection.setName(request.name());
        collection.setVisibility(request.visibility());
        return mapper.toResponse(collectionRepository.save(collection));
    }

    @Transactional
    public void delete(Long id) {
        AppUser user = currentUserService.getOrCreate();
        FlashcardCollection collection = findByIdOrThrow(id);
        ensureCanModify(collection, user);
        collectionRepository.delete(collection);
    }

    // --- helpers compartidos con FlashcardService/UsageService ---

    FlashcardCollection findByIdOrThrow(Long id) {
        return collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coleccion no encontrada: " + id));
    }

    void ensureCanRead(FlashcardCollection collection, AppUser user) {
        if (collection.getVisibility() == Visibility.PUBLIC) {
            return;
        }
        if (!isOwner(collection, user) && !isAdmin(user)) {
            throw new ResourceNotFoundException("Coleccion no encontrada: " + collection.getId());
        }
    }

    void ensureCanModify(FlashcardCollection collection, AppUser user) {
        if (!isOwner(collection, user) && !isAdmin(user)) {
            throw new ForbiddenOperationException("No tienes permiso sobre esta coleccion");
        }
    }

    private void validateVisibility(Visibility visibility, AppUser user) {
        if (visibility == Visibility.PUBLIC && ROLE_ESTUDIANTE.equals(user.getRole())) {
            throw new ForbiddenOperationException("Un estudiante solo puede crear colecciones privadas");
        }
    }

    boolean isAdmin(AppUser user) {
        return ROLE_ADMIN.equals(user.getRole());
    }

    private boolean isOwner(FlashcardCollection collection, AppUser user) {
        return collection.getAuthor().getId().equals(user.getId());
    }
}
