-- Estado y agenda de repeticion espaciada por usuario y flash card.
-- El historial de uso (flashcard_usage) se conserva intacto.
CREATE TABLE IF NOT EXISTS flashcard_review (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users (id),
    flashcard_id BIGINT NOT NULL REFERENCES flashcards (id) ON DELETE CASCADE,
    state VARCHAR(20) NOT NULL,
    repetitions INT NOT NULL DEFAULT 0,
    interval_days INT NOT NULL DEFAULT 0,
    ease_factor DOUBLE PRECISION NOT NULL DEFAULT 2.5,
    lapses INT NOT NULL DEFAULT 0,
    due_at TIMESTAMP NOT NULL,
    last_reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_flashcard_review_user_card UNIQUE (user_id, flashcard_id)
);

CREATE INDEX IF NOT EXISTS idx_flashcard_review_user_due ON flashcard_review (user_id, due_at);
CREATE INDEX IF NOT EXISTS idx_flashcard_review_flashcard ON flashcard_review (flashcard_id);
