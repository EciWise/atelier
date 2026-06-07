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
import java.util.Set;

@Service
public class CollectionService {

    static final String ROLE_ADMIN = "admin";
    static final String ROLE_ESTUDIANTE = "estudiante";

    private final FlashcardCollectionRepository collectionRepository;
    private final CollectionFavoriteRepository favoriteRepository;
    private final CurrentUserService currentUserService;
    private final FlashcardMapper mapper;

    public CollectionService(FlashcardCollectionRepository collectionRepository,
                             CollectionFavoriteRepository favoriteRepository,
                             CurrentUserService currentUserService,
                             FlashcardMapper mapper) {
        this.collectionRepository = collectionRepository;
        this.favoriteRepository = favoriteRepository;
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

        // Una coleccion recien creada no es favorita todavia.
        return mapper.toResponse(collectionRepository.save(collection), false);
    }

    @Transactional
    public List<CollectionResponse> listVisible() {
        AppUser user = currentUserService.getOrCreate();
        List<FlashcardCollection> collections = isAdmin(user)
                ? collectionRepository.findAll()
                : collectionRepository.findVisibleTo(user.getId());
        Set<Long> favoriteIds = favoriteRepository.findCollectionIdsByUserId(user.getId());
        return collections.stream()
                .map(c -> mapper.toResponse(c, favoriteIds.contains(c.getId())))
                .toList();
    }

    @Transactional
    public CollectionResponse get(Long id) {
        AppUser user = currentUserService.getOrCreate();
        FlashcardCollection collection = findByIdOrThrow(id);
        ensureCanRead(collection, user);
        boolean favorite = favoriteRepository.existsByUser_IdAndCollection_Id(user.getId(), id);
        return mapper.toResponse(collection, favorite);
    }

    @Transactional
    public CollectionResponse update(Long id, CollectionRequest request) {
        AppUser user = currentUserService.getOrCreate();
        FlashcardCollection collection = findByIdOrThrow(id);
        ensureCanModify(collection, user);
        validateVisibility(request.visibility(), user);

        collection.setName(request.name());
        collection.setVisibility(request.visibility());
        boolean favorite = favoriteRepository.existsByUser_IdAndCollection_Id(user.getId(), id);
        return mapper.toResponse(collectionRepository.save(collection), favorite);
    }

    @Transactional
    public CollectionResponse setFavorite(Long id, boolean favorite) {
        AppUser user = currentUserService.getOrCreate();
        FlashcardCollection collection = findByIdOrThrow(id);
        // Cualquiera que pueda ver la coleccion puede fijarla (incluidas las publicas ajenas).
        ensureCanRead(collection, user);
        if (favorite) {
            if (!favoriteRepository.existsByUser_IdAndCollection_Id(user.getId(), id)) {
                favoriteRepository.save(CollectionFavorite.builder()
                        .user(user)
                        .collection(collection)
                        .build());
            }
        } else {
            favoriteRepository.deleteByUser_IdAndCollection_Id(user.getId(), id);
        }
        return mapper.toResponse(collection, favorite);
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
