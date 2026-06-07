package com.eciwise.study.quiz;

import com.eciwise.study.exception.ForbiddenOperationException;
import com.eciwise.study.exception.ResourceNotFoundException;
import com.eciwise.study.quiz.dto.AnswerRequest;
import com.eciwise.study.quiz.dto.AnswerResultResponse;
import com.eciwise.study.quiz.dto.PagedResponse;
import com.eciwise.study.quiz.dto.ParcialParams;
import com.eciwise.study.quiz.dto.SessionResponse;
import com.eciwise.study.quiz.dto.SessionSummaryResponse;
import com.eciwise.study.quiz.dto.StartSessionRequest;
import com.eciwise.study.subject.Subject;
import com.eciwise.study.subject.SubjectService;
import com.eciwise.study.user.AppUser;
import com.eciwise.study.user.CurrentUserService;
import com.eciwise.study.user.Roles;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Motor de los tres modos de quiz. Garantiza: el usuario se auto-provisiona, solo el
 * dueno opera su sesion, una pregunta no se responde dos veces, y la respuesta correcta
 * solo se revela al contestar. Mantiene contadores, score y vidas, y cierra la sesion
 * cuando corresponde (sin vidas en Supervivencia, o al completar el set en Parcial/Repaso).
 */
@Service
public class QuizService {

    static final int SURVIVAL_LIVES = 3;

    private final QuizSessionRepository sessionRepository;
    private final QuizAnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;
    private final QuestionCollectionItemRepository collectionItemRepository;
    private final QuestionService questionService;
    private final QuestionCollectionService collectionService;
    private final SubjectService subjectService;
    private final CurrentUserService currentUserService;
    private final ParcialSelector parcialSelector;
    private final QuizScorer scorer;
    private final QuizMapper mapper;

    public QuizService(QuizSessionRepository sessionRepository,
                       QuizAnswerRepository answerRepository,
                       QuestionRepository questionRepository,
                       QuestionOptionRepository optionRepository,
                       QuestionCollectionItemRepository collectionItemRepository,
                       QuestionService questionService,
                       QuestionCollectionService collectionService,
                       SubjectService subjectService,
                       CurrentUserService currentUserService,
                       ParcialSelector parcialSelector,
                       QuizScorer scorer,
                       QuizMapper mapper) {
        this.sessionRepository = sessionRepository;
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.collectionItemRepository = collectionItemRepository;
        this.questionService = questionService;
        this.collectionService = collectionService;
        this.subjectService = subjectService;
        this.currentUserService = currentUserService;
        this.parcialSelector = parcialSelector;
        this.scorer = scorer;
        this.mapper = mapper;
    }

    @Transactional
    public SessionResponse start(StartSessionRequest request) {
        AppUser user = currentUserService.getOrCreate();
        Instant now = Instant.now();

        QuizSession session = QuizSession.builder()
                .user(user)
                .mode(request.mode())
                .status(SessionStatus.IN_PROGRESS)
                .startedAt(now)
                .build();

        List<Question> questions = switch (request.mode()) {
            case PARCIAL -> startParcial(request, user, session);
            case REPASO -> startRepaso(request, session);
            case SUPERVIVENCIA -> startSurvival(session);
        };

        QuizSession saved = sessionRepository.save(session);
        return new SessionResponse(
                saved.getId(),
                saved.getMode(),
                saved.getStatus(),
                saved.getTotalQuestions(),
                saved.getLivesRemaining(),
                saved.getSubject() == null ? null : saved.getSubject().getId(),
                saved.getCorte(),
                saved.getCollection() == null ? null : saved.getCollection().getId(),
                saved.getStartedAt(),
                questions.stream().map(mapper::toQuizQuestion).toList()
        );
    }

    private List<Question> startParcial(StartSessionRequest request, AppUser user, QuizSession session) {
        if (request.subjectId() == null || request.corte() == null || request.parcial() == null) {
            throw new ForbiddenOperationException("Parcial requiere materia, corte y parametros");
        }
        if (request.corte() < 1 || request.corte() > 3) {
            throw new ForbiddenOperationException("El corte debe estar entre 1 y 3");
        }
        Subject subject = subjectService.findByIdOrThrow(request.subjectId());
        List<Question> pool = questionRepository.findBySubject_IdAndCorte(subject.getId(), request.corte());
        if (pool.isEmpty()) {
            throw new ResourceNotFoundException("No hay preguntas para esa materia y corte");
        }

        ParcialParams p = request.parcial();
        Set<Long> incorrect = new HashSet<>(
                answerRepository.incorrectQuestionIds(user.getId(), subject.getId(), request.corte()));
        Set<Long> answered = new HashSet<>(
                answerRepository.answeredQuestionIds(user.getId(), subject.getId(), request.corte()));
        int count = parcialSelector.desiredCount(
                p.daysUntilExam(), p.preparedness(), p.targetGrade(), pool.size());
        List<Question> selected = parcialSelector.select(pool, count, incorrect, answered);

        session.setSubject(subject);
        session.setCorte(request.corte());
        session.setDaysUntilExam(p.daysUntilExam());
        session.setPreparedness(p.preparedness());
        session.setTargetGrade(p.targetGrade());
        session.setTotalQuestions(selected.size());
        return selected;
    }

    private List<Question> startRepaso(StartSessionRequest request, QuizSession session) {
        if (request.collectionId() == null) {
            throw new ForbiddenOperationException("Repaso requiere una coleccion");
        }
        QuestionCollection collection = collectionService.findByIdOrThrow(request.collectionId());
        List<Question> questions = collection.getItems().stream()
                .map(QuestionCollectionItem::getQuestion)
                .toList();
        if (questions.isEmpty()) {
            throw new ResourceNotFoundException("La coleccion no tiene preguntas");
        }
        session.setCollection(collection);
        session.setTotalQuestions(questions.size());
        return questions;
    }

    private List<Question> startSurvival(QuizSession session) {
        List<Question> pool = questionRepository.findByAvailableForSurvivalTrue();
        if (pool.isEmpty()) {
            throw new ResourceNotFoundException("No hay preguntas disponibles para supervivencia");
        }
        java.util.ArrayList<Question> shuffled = new java.util.ArrayList<>(pool);
        Collections.shuffle(shuffled);
        session.setLivesRemaining(SURVIVAL_LIVES);
        session.setTotalQuestions(0);
        return shuffled;
    }

    @Transactional
    public AnswerResultResponse answer(Long sessionId, AnswerRequest request) {
        AppUser user = currentUserService.getOrCreate();
        QuizSession session = findSessionOrThrow(sessionId);
        ensureOwner(session, user);
        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            throw new ForbiddenOperationException("La sesion ya no esta en progreso");
        }

        Question question = questionService.findByIdOrThrow(request.questionId());
        ensureQuestionBelongsToSession(session, question);

        if (answerRepository.existsBySession_IdAndQuestion_Id(sessionId, question.getId())) {
            throw new ForbiddenOperationException("Esta pregunta ya fue respondida");
        }
        if (session.getMode() != QuizMode.SUPERVIVENCIA
                && answerRepository.countBySession_Id(sessionId) >= session.getTotalQuestions()) {
            throw new ForbiddenOperationException("Ya se completaron todas las preguntas de la sesion");
        }

        Grade grade = grade(question, request);
        int points = scorer.points(grade.correct(), request.timeTakenMs(), question.getTimeLimitSeconds());

        answerRepository.save(QuizAnswer.builder()
                .session(session)
                .question(question)
                .selectedOption(grade.selectedOption())
                .givenAnswer(request.givenAnswer())
                .correct(grade.correct())
                .timeTakenMs(request.timeTakenMs())
                .pointsAwarded(points)
                .answeredAt(Instant.now())
                .build());

        applyResultToSession(session, grade.correct(), points);
        sessionRepository.save(session);

        int totalAnswered = session.getCorrectCount() + session.getIncorrectCount();
        return new AnswerResultResponse(
                grade.correct(),
                grade.correctOptionId(),
                grade.correctAnswerText(),
                question.getExplanation(),
                points,
                session.getLivesRemaining(),
                session.getStatus(),
                session.getScore(),
                session.getCorrectCount(),
                session.getIncorrectCount(),
                totalAnswered
        );
    }

    @Transactional
    public SessionSummaryResponse finish(Long sessionId) {
        AppUser user = currentUserService.getOrCreate();
        QuizSession session = findSessionOrThrow(sessionId);
        ensureOwner(session, user);
        if (session.getStatus() == SessionStatus.IN_PROGRESS) {
            session.setStatus(SessionStatus.COMPLETED);
            session.setFinishedAt(Instant.now());
            sessionRepository.save(session);
        }
        return mapper.toSummary(session);
    }

    @Transactional
    public SessionSummaryResponse getSession(Long sessionId) {
        AppUser user = currentUserService.getOrCreate();
        QuizSession session = findSessionOrThrow(sessionId);
        if (!isOwner(session, user) && !Roles.isAdmin(user)) {
            throw new ResourceNotFoundException("Sesion no encontrada: " + sessionId);
        }
        return mapper.toSummary(session);
    }

    @Transactional
    public PagedResponse<SessionSummaryResponse> history(Pageable pageable) {
        AppUser user = currentUserService.getOrCreate();
        return PagedResponse.of(sessionRepository
                .findByUser_IdOrderByStartedAtDesc(user.getId(), pageable)
                .map(mapper::toSummary));
    }

    // --- internos ---

    private void applyResultToSession(QuizSession session, boolean correct, int points) {
        if (correct) {
            session.setCorrectCount(session.getCorrectCount() + 1);
            session.setScore(session.getScore() + points);
        } else {
            session.setIncorrectCount(session.getIncorrectCount() + 1);
        }

        if (session.getMode() == QuizMode.SUPERVIVENCIA) {
            session.setTotalQuestions(session.getTotalQuestions() + 1);
            if (!correct) {
                session.setLivesRemaining(session.getLivesRemaining() - 1);
                if (session.getLivesRemaining() <= 0) {
                    completeNow(session);
                }
            }
        } else {
            int answered = session.getCorrectCount() + session.getIncorrectCount();
            if (answered >= session.getTotalQuestions()) {
                completeNow(session);
            }
        }
    }

    private void completeNow(QuizSession session) {
        session.setStatus(SessionStatus.COMPLETED);
        session.setFinishedAt(Instant.now());
    }

    private Grade grade(Question question, AnswerRequest request) {
        return switch (question.getType()) {
            case CLOSED -> gradeClosed(question, request);
            case TRUE_FALSE -> gradeTrueFalse(question, request);
            case OPEN -> gradeOpen(question, request);
        };
    }

    private Grade gradeClosed(Question question, AnswerRequest request) {
        if (request.selectedOptionId() == null) {
            throw new ForbiddenOperationException("Debes seleccionar una opcion");
        }
        QuestionOption selected = optionRepository.findById(request.selectedOptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Opcion no encontrada"));
        if (!selected.getQuestion().getId().equals(question.getId())) {
            throw new ForbiddenOperationException("La opcion no pertenece a la pregunta");
        }
        Long correctOptionId = question.getOptions().stream()
                .filter(QuestionOption::isCorrect)
                .map(QuestionOption::getId)
                .findFirst()
                .orElse(null);
        return new Grade(selected.isCorrect(), selected, correctOptionId, null);
    }

    private Grade gradeTrueFalse(Question question, AnswerRequest request) {
        Boolean given = Answers.parseBoolean(request.givenAnswer());
        boolean correct = given != null && given.toString().equals(question.getCorrectAnswer());
        return new Grade(correct, null, null, question.getCorrectAnswer());
    }

    private Grade gradeOpen(Question question, AnswerRequest request) {
        String given = Answers.normalize(request.givenAnswer());
        boolean correct = !given.isEmpty()
                && given.equals(Answers.normalize(question.getCorrectAnswer()));
        return new Grade(correct, null, null, question.getCorrectAnswer());
    }

    private void ensureQuestionBelongsToSession(QuizSession session, Question question) {
        boolean belongs = switch (session.getMode()) {
            case PARCIAL -> session.getSubject() != null
                    && question.getSubject().getId().equals(session.getSubject().getId())
                    && question.getCorte().equals(session.getCorte());
            case REPASO -> session.getCollection() != null
                    && collectionItemRepository.existsByCollection_IdAndQuestion_Id(
                    session.getCollection().getId(), question.getId());
            case SUPERVIVENCIA -> question.isAvailableForSurvival();
        };
        if (!belongs) {
            throw new ForbiddenOperationException("La pregunta no pertenece a esta sesion");
        }
    }

    private QuizSession findSessionOrThrow(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sesion no encontrada: " + id));
    }

    private void ensureOwner(QuizSession session, AppUser user) {
        if (!isOwner(session, user)) {
            throw new ResourceNotFoundException("Sesion no encontrada: " + session.getId());
        }
    }

    private boolean isOwner(QuizSession session, AppUser user) {
        return session.getUser().getId().equals(user.getId());
    }

    /** Resultado de calificar una respuesta junto con la verdad a revelar. */
    private record Grade(boolean correct, QuestionOption selectedOption,
                         Long correctOptionId, String correctAnswerText) {
    }
}
