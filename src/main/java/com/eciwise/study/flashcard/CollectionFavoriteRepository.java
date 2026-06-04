package com.eciwise.study.flashcard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface CollectionFavoriteRepository extends JpaRepository<CollectionFavorite, Long> {

    boolean existsByUser_IdAndCollection_Id(Long userId, Long collectionId);

    void deleteByUser_IdAndCollection_Id(Long userId, Long collectionId);

    /**
     * Ids de las colecciones favoritas de un usuario, en una sola consulta
     * (evita N+1 al mapear el listado de colecciones).
     */
    @Query("select f.collection.id from CollectionFavorite f where f.user.id = :userId")
    Set<Long> findCollectionIdsByUserId(@Param("userId") Long userId);
}
