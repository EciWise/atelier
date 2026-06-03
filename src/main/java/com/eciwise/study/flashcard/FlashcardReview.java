package com.eciwise.study.flashcard;

import com.eciwise.study.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Estado y agenda de repeticion espaciada de una flash card para un usuario.
 * Existe una unica fila por par (usuario, flashcard); el algoritmo que la
 * actualiza vive en {@link SpacedRepetitionScheduler}.
 */
@Entity
@Table(
        name = "flashcard_review",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_flashcard_review_user_card",
                columnNames = {"user_id", "flashcard_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class FlashcardReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReviewState state = ReviewState.EN_APRENDIZAJE;

    /** Repasos correctos consecutivos (se reinicia con REPETIR). */
    @Column(nullable = false)
    @Builder.Default
    private int repetitions = 0;

    /** Intervalo actual en dias hasta el proximo repaso. */
    @Column(name = "interval_days", nullable = false)
    @Builder.Default
    private int intervalDays = 0;

    /** Factor de facilidad SM-2 (minimo 1.3). */
    @Column(name = "ease_factor", nullable = false)
    @Builder.Default
    private double easeFactor = 2.5;

    /** Numero de veces que el usuario ha pulsado REPETIR sobre esta tarjeta. */
    @Column(nullable = false)
    @Builder.Default
    private int lapses = 0;

    @Column(name = "due_at", nullable = false)
    private Instant dueAt;

    @Column(name = "last_reviewed_at")
    private Instant lastReviewedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
