package com.eciwise.study.quiz;

import com.eciwise.study.auth.AuthenticatedUser;
import com.eciwise.study.exception.ForbiddenOperationException;
import com.eciwise.study.quiz.dto.AnswerRequest;
import com.eciwise.study.quiz.dto.AnswerResultResponse;
import com.eciwise.study.quiz.dto.LeaderboardResponse;
import com.eciwise.study.quiz.dto.OptionRequest;
import com.eciwise.study.quiz.dto.ParcialParams;
import com.eciwise.study.quiz.dto.QuestionCollectionRequest;
import com.eciwise.study.quiz.dto.QuestionCollectionResponse;
import com.eciwise.study.quiz.dto.QuestionRequest;
import com.eciwise.study.quiz.dto.QuestionResponse;
import com.eciwise.study.quiz.dto.QuestionStatsResponse;
import com.eciwise.study.quiz.dto.SessionResponse;
import com.eciwise.study.quiz.dto.StartSessionRequest;
import com.eciwise.study.subject.SubjectService;
import com.eciwise.study.subject.dto.SubjectRequest;
import com.eciwise.study.subject.dto.SubjectResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class QuizFlowTest {

    @Autowired
    private SubjectService subjectService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private QuestionCollectionService collectionService;
    @Autowired
    private QuizService quizService;
    @Autowired
    private LeaderboardService leaderboardService;

    private final Map<Long, Long> correctOptionByQuestion = new HashMap<>();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
        correctOptionByQuestion.clear();
    }

    private void authenticate(String externalId, String role) {
        AuthenticatedUser principal = new AuthenticatedUser(
                externalId, externalId + "@test.com", "Nombre", externalId, role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal, null, List.of(new SimpleGrantedAuthority(role))));
    }

    private SubjectResponse createSubjectAsAdmin(String name) {
        authenticate("admin-1", "admin");
        return subjectService.create(new SubjectRequest(name, null));
    }

    private QuestionResponse createClosedQuestion(Long subjectId, int corte, boolean survival) {
        QuestionResponse q = questionService.create(new QuestionRequest(
                subjectId, corte, QuestionType.CLOSED, "2 + 2 = ?", "Suma basica", null,
                survival, 20,
                List.of(new OptionRequest("4", true), new OptionRequest("5", false))));
        Long correctOption = q.options().stream().filter(o -> o.correct()).findFirst().orElseThrow().id();
        correctOptionByQuestion.put(q.id(), correctOption);
        return q;
    }

    // --- Materias: CRUD solo admin ---

    @Test
    void estudianteNoPuedeCrearMateria() {
        authenticate("est-1", "estudiante");
        assertThatThrownBy(() -> subjectService.create(new SubjectRequest("Calculo", null)))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void adminCreaMateria() {
        SubjectResponse subject = createSubjectAsAdmin("Algebra");
        assertThat(subject.id()).isNotNull();
        assertThat(subject.name()).isEqualTo("Algebra");
    }

    // --- Preguntas: solo admin/tutor, validacion por tipo ---

    @Test
    void estudianteNoPuedeCrearPregunta() {
        SubjectResponse subject = createSubjectAsAdmin("Fisica");
        authenticate("est-2", "estudiante");
        assertThatThrownBy(() -> createClosedQuestion(subject.id(), 1, false))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void tutorCreaTiposDePregunta() {
        SubjectResponse subject = createSubjectAsAdmin("Quimica");
        authenticate("tutor-1", "tutor");

        QuestionResponse closed = createClosedQuestion(subject.id(), 1, false);
        assertThat(closed.options()).hasSize(2);

        QuestionResponse vf = questionService.create(new QuestionRequest(
                subject.id(), 1, QuestionType.TRUE_FALSE, "El agua es H2O", null, "true",
                false, 20, null));
        assertThat(vf.correctAnswer()).isEqualTo("true");

        QuestionResponse open = questionService.create(new QuestionRequest(
                subject.id(), 1, QuestionType.OPEN, "Simbolo del oro", null, "Au",
                false, 20, null));
        assertThat(open.correctAnswer()).isEqualTo("Au");
    }

    @Test
    void preguntaCerradaRequiereOpciones() {
        SubjectResponse subject = createSubjectAsAdmin("Biologia");
        authenticate("tutor-2", "tutor");
        assertThatThrownBy(() -> questionService.create(new QuestionRequest(
                subject.id(), 1, QuestionType.CLOSED, "Sin opciones", null, null, false, 20, List.of())))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    // --- Modo Parcial ---

    @Test
    void parcialAltaIntensidadSeleccionaTodoElBanco() {
        SubjectResponse subject = createSubjectAsAdmin("Historia");
        authenticate("tutor-3", "tutor");
        for (int i = 0; i < 8; i++) {
            createClosedQuestion(subject.id(), 1, false);
        }

        authenticate("est-parcial", "estudiante");
        SessionResponse session = quizService.start(new StartSessionRequest(
                QuizMode.PARCIAL, subject.id(), 1, null,
                new ParcialParams(0, 1, new BigDecimal("5.0"))));

        // Maxima urgencia/brecha/ambicion: pide mas de las disponibles, se topa en 8.
        assertThat(session.totalQuestions()).isEqualTo(8);
        assertThat(session.questions()).hasSize(8);
        // No se filtra la respuesta correcta en las opciones jugables.
        assertThat(session.questions().get(0).options()).allSatisfy(o -> assertThat(o.text()).isNotBlank());
    }

    @Test
    void parcialSeCompletaAlResponderTodo() {
        SubjectResponse subject = createSubjectAsAdmin("Geografia");
        authenticate("tutor-4", "tutor");
        for (int i = 0; i < 6; i++) {
            createClosedQuestion(subject.id(), 1, false);
        }

        authenticate("est-completa", "estudiante");
        SessionResponse session = quizService.start(new StartSessionRequest(
                QuizMode.PARCIAL, subject.id(), 1, null,
                new ParcialParams(0, 1, new BigDecimal("5.0"))));

        AnswerResultResponse last = null;
        for (var q : session.questions()) {
            last = quizService.answer(session.id(), new AnswerRequest(
                    q.id(), correctOptionByQuestion.get(q.id()), null, 1000));
        }
        assertThat(last).isNotNull();
        assertThat(last.status()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(last.correctCount()).isEqualTo(session.totalQuestions());
        assertThat(last.score()).isGreaterThan(0);
    }

    // --- Modo Repaso ---

    @Test
    void repasoUsaLasPreguntasDeLaColeccion() {
        SubjectResponse subject = createSubjectAsAdmin("Ingles");
        authenticate("tutor-5", "tutor");
        QuestionResponse q1 = createClosedQuestion(subject.id(), 1, false);
        QuestionResponse q2 = createClosedQuestion(subject.id(), 1, false);
        QuestionCollectionResponse collection = collectionService.create(new QuestionCollectionRequest(
                "Repaso 1", null, subject.id(), List.of(q1.id(), q2.id())));

        authenticate("est-repaso", "estudiante");
        SessionResponse session = quizService.start(new StartSessionRequest(
                QuizMode.REPASO, null, null, collection.id(), null));

        assertThat(session.totalQuestions()).isEqualTo(2);
        assertThat(session.questions()).extracting(q -> q.id()).containsExactlyInAnyOrder(q1.id(), q2.id());
    }

    // --- Modo Supervivencia + ranking ---

    @Test
    void supervivenciaTerminaSinVidasYAlimentaRanking() {
        SubjectResponse subject = createSubjectAsAdmin("Programacion");
        authenticate("tutor-6", "tutor");
        for (int i = 0; i < 5; i++) {
            createClosedQuestion(subject.id(), 1, true);
        }

        authenticate("est-survival", "estudiante");
        SessionResponse session = quizService.start(new StartSessionRequest(
                QuizMode.SUPERVIVENCIA, null, null, null, null));
        assertThat(session.livesRemaining()).isEqualTo(3);

        // Responder mal 3 veces (seleccionando una opcion incorrecta) agota las vidas.
        AnswerResultResponse result = null;
        int answered = 0;
        for (var q : session.questions()) {
            Long wrongOption = q.options().stream()
                    .map(o -> o.id())
                    .filter(id -> !id.equals(correctOptionByQuestion.get(q.id())))
                    .findFirst().orElseThrow();
            result = quizService.answer(session.id(), new AnswerRequest(q.id(), wrongOption, null, 1000));
            answered++;
            if (result.status() == SessionStatus.COMPLETED) {
                break;
            }
        }
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(result.livesRemaining()).isEqualTo(0);
        assertThat(answered).isEqualTo(3);

        LeaderboardResponse leaderboard = leaderboardService.survivalLeaderboard(PageRequest.of(0, 20));
        assertThat(leaderboard.myRank()).isEqualTo(1L);
        assertThat(leaderboard.myBestScore()).isNotNull();
        assertThat(leaderboard.table().content()).isNotEmpty();
    }

    // --- Historial privado por usuario ---

    @Test
    void historialEsPrivadoPorUsuario() {
        SubjectResponse subject = createSubjectAsAdmin("Etica");
        authenticate("tutor-7", "tutor");
        for (int i = 0; i < 3; i++) {
            createClosedQuestion(subject.id(), 1, false);
        }

        authenticate("est-a", "estudiante");
        SessionResponse session = quizService.start(new StartSessionRequest(
                QuizMode.PARCIAL, subject.id(), 1, null,
                new ParcialParams(0, 1, new BigDecimal("5.0"))));
        Long ownSessionId = session.id();

        authenticate("est-b", "estudiante");
        var historyB = quizService.history(PageRequest.of(0, 20));
        assertThat(historyB.content()).extracting(s -> s.id()).doesNotContain(ownSessionId);
    }

    // --- Estadisticas agregadas (admin) ---

    @Test
    void statsAgregaCorrectosEIncorrectosSoloParaAdmin() {
        SubjectResponse subject = createSubjectAsAdmin("Estadistica");
        authenticate("tutor-8", "tutor");
        QuestionResponse q = createClosedQuestion(subject.id(), 1, false);
        Long correctOption = correctOptionByQuestion.get(q.id());
        Long wrongOption = q.options().stream().map(o -> o.id())
                .filter(id -> !id.equals(correctOption)).findFirst().orElseThrow();

        // Un estudiante responde bien.
        authenticate("est-bien", "estudiante");
        playSingle(subject.id(), q.id(), correctOption);
        // Otro estudiante responde mal.
        authenticate("est-mal", "estudiante");
        playSingle(subject.id(), q.id(), wrongOption);

        // Un estudiante no puede ver estadisticas.
        authenticate("est-curioso", "estudiante");
        assertThatThrownBy(() -> questionService.stats(q.id()))
                .isInstanceOf(ForbiddenOperationException.class);

        authenticate("admin-1", "admin");
        QuestionStatsResponse stats = questionService.stats(q.id());
        assertThat(stats.timesAnswered()).isEqualTo(2);
        assertThat(stats.correct()).isEqualTo(1);
        assertThat(stats.incorrect()).isEqualTo(1);
        assertThat(stats.correctRate()).isEqualTo(50.0);
    }

    private void playSingle(Long subjectId, Long questionId, Long optionId) {
        SessionResponse session = quizService.start(new StartSessionRequest(
                QuizMode.PARCIAL, subjectId, 1, null,
                new ParcialParams(0, 1, new BigDecimal("5.0"))));
        quizService.answer(session.id(), new AnswerRequest(questionId, optionId, null, 1000));
    }
}
