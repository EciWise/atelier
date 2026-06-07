-- Banco de preguntas tipo quiz: materias, preguntas (cerradas/abiertas/V-F),
-- opciones y colecciones de preguntas para el modo Repaso.

CREATE TABLE IF NOT EXISTS subjects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS questions (
    id BIGSERIAL PRIMARY KEY,
    subject_id BIGINT NOT NULL REFERENCES subjects (id),
    corte INT NOT NULL,
    type VARCHAR(20) NOT NULL,
    statement TEXT NOT NULL,
    explanation TEXT,
    correct_answer TEXT,
    available_for_survival BOOLEAN NOT NULL DEFAULT FALSE,
    time_limit_seconds INT NOT NULL DEFAULT 20,
    author_id BIGINT NOT NULL REFERENCES app_users (id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT ck_questions_corte CHECK (corte BETWEEN 1 AND 3)
);

CREATE INDEX IF NOT EXISTS idx_questions_subject_corte ON questions (subject_id, corte);
CREATE INDEX IF NOT EXISTS idx_questions_survival ON questions (available_for_survival);

CREATE TABLE IF NOT EXISTS question_options (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL REFERENCES questions (id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    position INT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_question_options_question ON question_options (question_id);

CREATE TABLE IF NOT EXISTS question_collections (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    subject_id BIGINT REFERENCES subjects (id),
    author_id BIGINT NOT NULL REFERENCES app_users (id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_question_collections_author ON question_collections (author_id);

CREATE TABLE IF NOT EXISTS question_collection_items (
    id BIGSERIAL PRIMARY KEY,
    collection_id BIGINT NOT NULL REFERENCES question_collections (id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES questions (id) ON DELETE CASCADE,
    position INT,
    CONSTRAINT uq_collection_item UNIQUE (collection_id, question_id)
);

CREATE INDEX IF NOT EXISTS idx_collection_items_collection ON question_collection_items (collection_id);
CREATE INDEX IF NOT EXISTS idx_collection_items_question ON question_collection_items (question_id);
