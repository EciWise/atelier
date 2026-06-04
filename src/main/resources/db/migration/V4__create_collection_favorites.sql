-- Colecciones marcadas como favoritas ("fijadas") por cada usuario.
-- Una unica fila por par (usuario, coleccion); permite que la pestana "Estudiar"
-- liste solo las colecciones que el usuario eligio.
CREATE TABLE IF NOT EXISTS collection_favorite (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users (id),
    collection_id BIGINT NOT NULL REFERENCES flashcard_collections (id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_collection_favorite_user_collection UNIQUE (user_id, collection_id)
);

CREATE INDEX IF NOT EXISTS idx_collection_favorite_user ON collection_favorite (user_id);
CREATE INDEX IF NOT EXISTS idx_collection_favorite_collection ON collection_favorite (collection_id);
