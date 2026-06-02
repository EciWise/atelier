-- El CRUD demo de students se elimina; se reemplaza por usuarios auto-provisionados desde el JWT.
DROP TABLE IF EXISTS students;

CREATE TABLE IF NOT EXISTS app_users (
    id BIGSERIAL PRIMARY KEY,
    external_id VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS flashcard_collections (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    author_id BIGINT NOT NULL REFERENCES app_users (id),
    visibility VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_flashcard_collections_author ON flashcard_collections (author_id);

CREATE TABLE IF NOT EXISTS flashcards (
    id BIGSERIAL PRIMARY KEY,
    collection_id BIGINT NOT NULL REFERENCES flashcard_collections (id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_flashcards_collection ON flashcards (collection_id);

CREATE TABLE IF NOT EXISTS flashcard_usage (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users (id),
    flashcard_id BIGINT NOT NULL REFERENCES flashcards (id) ON DELETE CASCADE,
    used_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_flashcard_usage_user ON flashcard_usage (user_id);
CREATE INDEX IF NOT EXISTS idx_flashcard_usage_flashcard ON flashcard_usage (flashcard_id);
