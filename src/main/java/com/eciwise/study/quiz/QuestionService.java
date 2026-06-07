package com.eciwise.study.quiz;

import com.eciwise.study.exception.ForbiddenOperationException;
import com.eciwise.study.exception.ResourceNotFoundException;
import com.eciwise.study.quiz.dto.OptionRequest;
import com.eciwise.study.quiz.dto.QuestionAnswerStats;
import com.eciwise.study.quiz.dto.QuestionRequest;
import com.eciwise.study.quiz.dto.QuestionResponse;
import com.eciwise.study.quiz.dto.QuestionStatsResponse;
import com.eciwise.study.subject.Subject;
import com.eciwise.study.subject.SubjectService;
import com.eciwise.study.user.AppUser;
import com.eciwise.study.user.CurrentUserService;
import com.eciwise.study.user.Roles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Banco de preguntas. Crear/editar/eliminar es exclusivo de admin o tutor.
 * Las vistas exponen la respuesta correcta (gestion); el juego usa QuizService,
 * que nunca filtra la verdad. Las estadisticas agregadas son solo para admin.
 */
@Service
public class QuestionService {

    static final int DEFAULT_TIME_LIMIT = 20;

    private final QuestionRepository questionRepository;
    private final QuizAnswerRepository answerRepository;
    private final SubjectService subjectService;
    private final CurrentUserService currentUserService;
    private final QuizMapper mapper;

    public QuestionService(QuestionRepository questionRepository,
                           QuizAnswerRepository answerRepository,
                           SubjectService subjectService,
                           CurrentUserService currentUserService,
                           QuizMapper mapper) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.subjectService = subjectService;
        this.currentUserService = currentUserService;
        this.mapper = mapper;
    }

    @Transactional
    public QuestionResponse create(QuestionRequest request) {
        AppUser user = ensureAdminOrTutor();
        Subject subject = subjectService.findByIdOrThrow(request.subjectId());

        Question question = Question.builder()
                .subject(subject)
                .corte(request.corte())
                .type(request.type())
                .statement(request.statement())
                .explanation(request.explanation())
                .availableForSurvival(request.availableForSurvival())
                .timeLimitSeconds(request.timeLimitSeconds() == null ? DEFAULT_TIME_LIMIT : request.timeLimitSeconds())
                .author(user)
                .build();

        applyTypeData(question, request);
        return mapper.toResponse(questionRepository.save(question));
    }

    @Transactional
    public QuestionResponse update(Long id, QuestionRequest request) {
        ensureAdminOrTutor();
        Question question = findByIdOrThrow(id);
        Subject subject = subjectService.findByIdOrThrow(request.subjectId());

        question.setSubject(subject);
        question.setCorte(request.corte());
        question.setType(request.type());
        question.setStatement(request.statement());
        question.setExplanation(request.explanation());
        question.setAvailableForSurvival(request.availableForSurvival());
        question.setTimeLimitSeconds(request.timeLimitSeconds() == null ? DEFAULT_TIME_LIMIT : request.timeLimitSeconds());
        question.getOptions().clear();
        question.setCorrectAnswer(null);

        applyTypeData(question, request);
        return mapper.toResponse(questionRepository.save(question));
    }

    @Transactional
    public void delete(Long id) {
        ensureAdminOrTutor();
        questionRepository.delete(findByIdOrThrow(id));
    }

    @Transactional
    public QuestionResponse get(Long id) {
        ensureAdminOrTutor();
        return mapper.toResponse(findByIdOrThrow(id));
    }

    @Transactional
    public List<QuestionResponse> listBySubject(Long subjectId, Integer corte) {
        ensureAdminOrTutor();
        subjectService.findByIdOrThrow(subjectId);
        List<Question> questions = corte == null
                ? questionRepository.findBySubject_Id(subjectId)
                : questionRepository.findBySubject_IdAndCorte(subjectId, corte);
        return questions.stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public QuestionStatsResponse stats(Long id) {
        AppUser user = currentUserService.getOrCreate();
        if (!Roles.isAdmin(user)) {
            throw new ForbiddenOperationException("Solo un admin puede ver las estadisticas");
        }
        findByIdOrThrow(id);
        QuestionAnswerStats raw = answerRepository.statsByQuestion(id);
        long total = raw == null || raw.total() == null ? 0 : raw.total();
        long correct = raw == null || raw.correct() == null ? 0 : raw.correct();
        long incorrect = total - correct;
        double rate = total == 0 ? 0.0 : (correct * 100.0) / total;
        return new QuestionStatsResponse(id, total, correct, incorrect, Math.round(rate * 100.0) / 100.0);
    }

    Question findByIdOrThrow(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pregunta no encontrada: " + id));
    }

    /** Valida y persiste los datos especificos del tipo de pregunta. */
    private void applyTypeData(Question question, QuestionRequest request) {
        switch (request.type()) {
            case CLOSED -> {
                List<OptionRequest> options = request.options();
                if (options == null || options.size() < 2) {
                    throw new ForbiddenOperationException("Una pregunta cerrada requiere al menos 2 opciones");
                }
                long correct = options.stream().filter(OptionRequest::correct).count();
                if (correct < 1) {
                    throw new ForbiddenOperationException("Marca al menos una opcion correcta");
                }
                List<QuestionOption> entities = new ArrayList<>();
                for (int i = 0; i < options.size(); i++) {
                    OptionRequest o = options.get(i);
                    entities.add(QuestionOption.builder()
                            .question(question)
                            .text(o.text())
                            .correct(o.correct())
                            .position(i)
                            .build());
                }
                question.getOptions().addAll(entities);
            }
            case TRUE_FALSE -> {
                Boolean value = Answers.parseBoolean(request.correctAnswer());
                if (value == null) {
                    throw new ForbiddenOperationException("La respuesta de verdadero/falso debe ser true o false");
                }
                question.setCorrectAnswer(value.toString());
            }
            case OPEN -> {
                if (request.correctAnswer() == null || request.correctAnswer().isBlank()) {
                    throw new ForbiddenOperationException("Una pregunta abierta requiere una respuesta correcta");
                }
                question.setCorrectAnswer(request.correctAnswer().trim());
            }
        }
    }

    private AppUser ensureAdminOrTutor() {
        AppUser user = currentUserService.getOrCreate();
        if (!Roles.isAdminOrTutor(user)) {
            throw new ForbiddenOperationException("Solo admin o tutor pueden gestionar preguntas");
        }
        return user;
    }
}
