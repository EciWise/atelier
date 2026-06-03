package com.eciwise.study.flashcard;

import com.eciwise.study.auth.AuthenticatedUser;
import com.eciwise.study.flashcard.dto.CollectionRequest;
import com.eciwise.study.flashcard.dto.CollectionResponse;
import com.eciwise.study.flashcard.dto.FlashcardRequest;
import com.eciwise.study.flashcard.dto.FlashcardResponse;
import com.eciwise.study.flashcard.dto.ReviewResponse;
import com.eciwise.study.flashcard.dto.ReviewSummaryResponse;
import com.eciwise.study.flashcard.dto.StudyCardResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ReviewFlowTest {

    @Autowired
    private CollectionService collectionService;
    @Autowired
    private FlashcardService flashcardService;
    @Autowired
    private ReviewService reviewService;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(String externalId, String role) {
        AuthenticatedUser principal = new AuthenticatedUser(
                externalId, externalId + "@test.com", "Nombre", "Apellido", role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal, null, List.of(new SimpleGrantedAuthority(role))));
    }

    private FlashcardResponse nuevaTarjetaPublica(String autor, String titulo) {
        authenticate(autor, "tutor");
        CollectionResponse col = collectionService.create(new CollectionRequest("Estudio", Visibility.PUBLIC));
        return flashcardService.create(col.id(),
                new FlashcardRequest(titulo, "Desc", "Pregunta?", "Respuesta"));
    }

    @Test
    void aceptableProgramaUnDiaYSacaLaTarjetaDeLaCola() {
        FlashcardResponse card = nuevaTarjetaPublica("rev-owner-1", "T1");
        Long collectionId = card.collectionId();

        authenticate("rev-est-1", "estudiante");
        // Antes de repasar, la tarjeta nueva aparece en la cola de estudio.
        assertThat(reviewService.studyQueue(collectionId))
                .extracting(s -> s.card().id())
                .contains(card.id());

        ReviewResponse response = reviewService.review(card.id(), ReviewGrade.ACEPTABLE);
        assertThat(response.state()).isEqualTo(ReviewState.EN_APRENDIZAJE);
        assertThat(response.intervalDays()).isEqualTo(1);

        // Ya no esta vencida -> sale de la cola.
        assertThat(reviewService.studyQueue(collectionId))
                .extracting(s -> s.card().id())
                .doesNotContain(card.id());
    }

    @Test
    void repetirDejaLaTarjetaEnEstadoRepetir() {
        FlashcardResponse card = nuevaTarjetaPublica("rev-owner-2", "T2");

        authenticate("rev-est-2", "estudiante");
        ReviewResponse response = reviewService.review(card.id(), ReviewGrade.REPETIR);

        assertThat(response.state()).isEqualTo(ReviewState.REPETIR);
        assertThat(response.lapses()).isEqualTo(1);
    }

    @Test
    void resumenCuentaPorEstado() {
        FlashcardResponse card1 = nuevaTarjetaPublica("rev-owner-3", "A");
        Long colId = card1.collectionId();
        authenticate("rev-owner-3", "tutor");
        FlashcardResponse card2 = flashcardService.create(colId,
                new FlashcardRequest("B", "d", "q", "a"));

        authenticate("rev-est-3", "estudiante");
        reviewService.review(card1.id(), ReviewGrade.ACEPTABLE);
        reviewService.review(card2.id(), ReviewGrade.REPETIR);

        ReviewSummaryResponse summary = reviewService.mySummary();
        assertThat(summary.total()).isEqualTo(2);
        assertThat(summary.enAprendizaje()).isEqualTo(1);
        assertThat(summary.repetir()).isEqualTo(1);
    }

    @Test
    void elProgresoEsIndependientePorUsuario() {
        FlashcardResponse card = nuevaTarjetaPublica("rev-owner-4", "T4");

        authenticate("rev-est-4a", "estudiante");
        reviewService.review(card.id(), ReviewGrade.APRENDIDO);
        // Este usuario ya no la tiene pendiente.
        assertThat(reviewService.studyQueue(card.collectionId()))
                .extracting(s -> s.card().id())
                .doesNotContain(card.id());

        // Otro usuario sigue viendola como nueva.
        authenticate("rev-est-4b", "estudiante");
        List<StudyCardResponse> queue = reviewService.studyQueue(card.collectionId());
        assertThat(queue).extracting(s -> s.card().id()).contains(card.id());
        assertThat(queue).filteredOn(s -> s.card().id().equals(card.id()))
                .allMatch(s -> s.state() == null);
    }
}
