package com.eciwise.study.quiz;

import com.eciwise.study.exception.ForbiddenOperationException;
import com.eciwise.study.exception.ResourceNotFoundException;
import com.eciwise.study.quiz.dto.QuestionCollectionRequest;
import com.eciwise.study.quiz.dto.QuestionCollectionResponse;
import com.eciwise.study.subject.Subject;
import com.eciwise.study.subject.SubjectService;
import com.eciwise.study.user.AppUser;
import com.eciwise.study.user.CurrentUserService;
import com.eciwise.study.user.Roles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Colecciones de preguntas para el modo Repaso. Las crean admin/tutor; editarlas o
 * eliminarlas requiere ser el autor o admin. El orden de las preguntas se conserva.
 */
@Service
public class QuestionCollectionService {

    private final QuestionCollectionRepository collectionRepository;
    private final QuestionRepository questionRepository;
    private final SubjectService subjectService;
    private final CurrentUserService currentUserService;
    private final QuizMapper mapper;

    public QuestionCollectionService(QuestionCollectionRepository collectionRepository,
                                     QuestionRepository questionRepository,
                                     SubjectService subjectService,
                                     CurrentUserService currentUserService,
                                     QuizMapper mapper) {
        this.collectionRepository = collectionRepository;
        this.questionRepository = questionRepository;
        this.subjectService = subjectService;
        this.currentUserService = currentUserService;
        this.mapper = mapper;
    }

    @Transactional
    public QuestionCollectionResponse create(QuestionCollectionRequest request) {
        AppUser user = ensureAdminOrTutor();
        QuestionCollection collection = QuestionCollection.builder()
                .name(request.name())
                .description(request.description())
                .subject(resolveSubject(request.subjectId()))
                .author(user)
                .build();
        replaceItems(collection, request.questionIds());
        return mapper.toResponse(collectionRepository.save(collection));
    }

    @Transactional
    public QuestionCollectionResponse update(Long id, QuestionCollectionRequest request) {
        AppUser user = currentUserService.getOrCreate();
        QuestionCollection collection = findByIdOrThrow(id);
        ensureCanModify(collection, user);
        collection.setName(request.name());
        collection.setDescription(request.description());
        collection.setSubject(resolveSubject(request.subjectId()));
        collection.getItems().clear();
        replaceItems(collection, request.questionIds());
        return mapper.toResponse(collectionRepository.save(collection));
    }

    @Transactional
    public List<QuestionCollectionResponse> list() {
        currentUserService.getOrCreate();
        return collectionRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public QuestionCollectionResponse get(Long id) {
        currentUserService.getOrCreate();
        return mapper.toResponse(findByIdOrThrow(id));
    }

    @Transactional
    public void delete(Long id) {
        AppUser user = currentUserService.getOrCreate();
        QuestionCollection collection = findByIdOrThrow(id);
        ensureCanModify(collection, user);
        collectionRepository.delete(collection);
    }

    QuestionCollection findByIdOrThrow(Long id) {
        return collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coleccion de preguntas no encontrada: " + id));
    }

    private Subject resolveSubject(Long subjectId) {
        return subjectId == null ? null : subjectService.findByIdOrThrow(subjectId);
    }

    private void replaceItems(QuestionCollection collection, List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return;
        }
        int position = 0;
        for (Long questionId : questionIds) {
            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pregunta no encontrada: " + questionId));
            collection.getItems().add(QuestionCollectionItem.builder()
                    .collection(collection)
                    .question(question)
                    .position(position++)
                    .build());
        }
    }

    private void ensureCanModify(QuestionCollection collection, AppUser user) {
        boolean owner = collection.getAuthor().getId().equals(user.getId());
        if (!owner && !Roles.isAdmin(user)) {
            throw new ForbiddenOperationException("No tienes permiso sobre esta coleccion");
        }
    }

    private AppUser ensureAdminOrTutor() {
        AppUser user = currentUserService.getOrCreate();
        if (!Roles.isAdminOrTutor(user)) {
            throw new ForbiddenOperationException("Solo admin o tutor pueden crear colecciones de preguntas");
        }
        return user;
    }
}
