package com.eciwise.study.subject;

import com.eciwise.study.exception.ForbiddenOperationException;
import com.eciwise.study.exception.ResourceNotFoundException;
import com.eciwise.study.subject.dto.SubjectRequest;
import com.eciwise.study.subject.dto.SubjectResponse;
import com.eciwise.study.user.AppUser;
import com.eciwise.study.user.CurrentUserService;
import com.eciwise.study.user.Roles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD de materias. La lectura esta abierta a cualquier rol autenticado, pero
 * crear/editar/eliminar es exclusivo de admin. Eliminar una materia con preguntas
 * asociadas se bloquea para no dejar el banco inconsistente.
 */
@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final CurrentUserService currentUserService;
    private final SubjectMapper mapper;

    public SubjectService(SubjectRepository subjectRepository,
                          CurrentUserService currentUserService,
                          SubjectMapper mapper) {
        this.subjectRepository = subjectRepository;
        this.currentUserService = currentUserService;
        this.mapper = mapper;
    }

    @Transactional
    public SubjectResponse create(SubjectRequest request) {
        ensureAdmin();
        if (subjectRepository.existsByNameIgnoreCase(request.name())) {
            throw new ForbiddenOperationException("Ya existe una materia con ese nombre");
        }
        Subject subject = Subject.builder()
                .name(request.name())
                .description(request.description())
                .build();
        return mapper.toResponse(subjectRepository.save(subject));
    }

    @Transactional
    public List<SubjectResponse> list() {
        currentUserService.getOrCreate();
        return subjectRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public SubjectResponse get(Long id) {
        currentUserService.getOrCreate();
        return mapper.toResponse(findByIdOrThrow(id));
    }

    @Transactional
    public SubjectResponse update(Long id, SubjectRequest request) {
        ensureAdmin();
        Subject subject = findByIdOrThrow(id);
        if (!subject.getName().equalsIgnoreCase(request.name())
                && subjectRepository.existsByNameIgnoreCase(request.name())) {
            throw new ForbiddenOperationException("Ya existe una materia con ese nombre");
        }
        subject.setName(request.name());
        subject.setDescription(request.description());
        return mapper.toResponse(subjectRepository.save(subject));
    }

    @Transactional
    public void delete(Long id) {
        ensureAdmin();
        Subject subject = findByIdOrThrow(id);
        subjectRepository.delete(subject);
    }

    public Subject findByIdOrThrow(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Materia no encontrada: " + id));
    }

    private void ensureAdmin() {
        AppUser user = currentUserService.getOrCreate();
        if (!Roles.isAdmin(user)) {
            throw new ForbiddenOperationException("Solo un admin puede administrar materias");
        }
    }
}
