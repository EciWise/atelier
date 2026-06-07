package com.eciwise.study.flashcard;

import com.eciwise.study.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Coleccion marcada como favorita ("fijada") por un usuario. Existe una unica
 * fila por par (usuario, coleccion); permite que cada usuario destaque las
 * colecciones que quiere estudiar sin afectar a los demas.
 */
@Entity
@Table(
        name = "collection_favorite",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_collection_favorite_user_collection",
                columnNames = {"user_id", "collection_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class CollectionFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_id", nullable = false)
    private FlashcardCollection collection;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
