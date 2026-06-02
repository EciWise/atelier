package com.eciwise.study.flashcard;

import com.eciwise.study.auth.AuthenticatedUser;
import com.eciwise.study.exception.ForbiddenOperationException;
import com.eciwise.study.exception.ResourceNotFoundException;
import com.eciwise.study.flashcard.dto.CollectionRequest;
import com.eciwise.study.flashcard.dto.CollectionResponse;
import com.eciwise.study.flashcard.dto.FlashcardRequest;
import com.eciwise.study.flashcard.dto.FlashcardResponse;
import com.eciwise.study.flashcard.dto.UsageSummaryResponse;
import com.eciwise.study.user.AppUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class FlashcardFlowTest {

    @Autowired
    private CollectionService collectionService;
    @Autowired
    private FlashcardService flashcardService;
    @Autowired
    private UsageService usageService;
    @Autowired
    private AppUserRepository appUserRepository;

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

    @Test
    void estudianteNoPuedeCrearColeccionPublica() {
        authenticate("est-1", "estudiante");
        assertThatThrownBy(() -> collectionService.create(new CollectionRequest("Mate", Visibility.PUBLIC)))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void primeraAccionAutoProvisionaElUsuario() {
        authenticate("est-auto", "estudiante");
        assertThat(appUserRepository.findByExternalId("est-auto")).isEmpty();

        collectionService.create(new CollectionRequest("Privada", Visibility.PRIVATE));

        assertThat(appUserRepository.findByExternalId("est-auto")).isPresent();
    }

    @Test
    void coleccionPrivadaNoEsVisibleParaTerceros() {
        authenticate("owner-1", "tutor");
        CollectionResponse priv = collectionService.create(new CollectionRequest("Secreta", Visibility.PRIVATE));

        authenticate("other-1", "estudiante");
        assertThat(collectionService.listVisible()).extracting(CollectionResponse::id).doesNotContain(priv.id());
        assertThatThrownBy(() -> collectionService.get(priv.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void coleccionPublicaEsVisibleParaTodos() {
        authenticate("tutor-1", "tutor");
        CollectionResponse pub = collectionService.create(new CollectionRequest("Abierta", Visibility.PUBLIC));

        authenticate("est-2", "estudiante");
        assertThat(collectionService.listVisible()).extracting(CollectionResponse::id).contains(pub.id());
        assertThat(collectionService.get(pub.id()).name()).isEqualTo("Abierta");
    }

    @Test
    void terceroNoPuedeBorrarPeroAdminSi() {
        authenticate("owner-2", "tutor");
        CollectionResponse col = collectionService.create(new CollectionRequest("Mia", Visibility.PUBLIC));

        authenticate("intruso", "estudiante");
        assertThatThrownBy(() -> collectionService.delete(col.id()))
                .isInstanceOf(ForbiddenOperationException.class);

        authenticate("admin-1", "admin");
        collectionService.delete(col.id());
        assertThatThrownBy(() -> collectionService.get(col.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void registraUsoYDevuelveHistorial() {
        authenticate("owner-3", "tutor");
        CollectionResponse col = collectionService.create(new CollectionRequest("Estudio", Visibility.PUBLIC));
        FlashcardResponse card = flashcardService.create(col.id(),
                new FlashcardRequest("Titulo", "Desc", "Pregunta?", "Respuesta"));

        authenticate("est-3", "estudiante");
        usageService.recordUsage(card.id());
        usageService.recordUsage(card.id());

        UsageSummaryResponse summary = usageService.mySummary();
        assertThat(summary.totalUsed()).isEqualTo(2);
        assertThat(summary.history()).hasSize(2);
        assertThat(summary.history().get(0).flashcardId()).isEqualTo(card.id());
    }
}
