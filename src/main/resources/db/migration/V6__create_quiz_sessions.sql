-- Sesiones de quiz (Parcial / Repaso / Supervivencia) y respuestas individuales.
-- El historial es por usuario; Supervivencia alimenta el ranking publico.

CREATE TABLE IF NOT EXISTS quiz_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users (id),
    mode VARCHAR(20) NOT NULL,
    subject_id BIGINT REFERENCES subjects (id),
    corte INT,
    collection_id BIGINT REFERENCES question_collections (id),
    status VARCHAR(20) NOT NULL,
    total_questions INT NOT NULL DEFAULT 0,
    correct_count INT NOT NULL DEFAULT 0,
    incorrect_count INT NOT NULL DEFAULT 0,
    score INT NOT NULL DEFAULT 0,
    lives_remaining INT,
    days_until_exam INT,
    preparedness INT,
    target_grade NUMERIC(4, 2),
    started_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_quiz_sessions_user ON quiz_sessions (user_id, started_at);
CREATE INDEX IF NOT EXISTS idx_quiz_sessions_ranking ON quiz_sessions (mode, status, score);

CREATE TABLE IF NOT EXISTS quiz_answers (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES quiz_sessions (id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES questions (id),
    selected_option_id BIGINT REFERENCES question_options (id),
    given_answer TEXT,
    is_correct BOOLEAN NOT NULL,
    time_taken_ms INT,
    points_awarded INT NOT NULL DEFAULT 0,
    answered_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_quiz_answer_session_question UNIQUE (session_id, question_id)
);

CREATE INDEX IF NOT EXISTS idx_quiz_answers_question ON quiz_answers (question_id);
CREATE INDEX IF NOT EXISTS idx_quiz_answers_session ON quiz_answers (session_id);
