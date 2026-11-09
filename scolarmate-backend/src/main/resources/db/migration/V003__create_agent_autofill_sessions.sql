CREATE TABLE IF NOT EXISTS agent_autofill_sessions (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    scholarship_id BIGINT,
    portal_name VARCHAR(120) NOT NULL,
    portal_url VARCHAR(1024),
    status VARCHAR(40) NOT NULL DEFAULT 'IN_PROGRESS',
    total_fields INT NOT NULL DEFAULT 0,
    filled_fields INT NOT NULL DEFAULT 0,
    accepted_fields INT NOT NULL DEFAULT 0,
    rejected_fields INT NOT NULL DEFAULT 0,
    missing_fields INT NOT NULL DEFAULT 0,
    completion_percentage INT NOT NULL DEFAULT 0,
    field_states JSONB NOT NULL DEFAULT '{}',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_agent_session_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_agent_session_scholarship FOREIGN KEY (scholarship_id) REFERENCES scholarships(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_agent_session_user ON agent_autofill_sessions(user_id, updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_agent_session_uuid ON agent_autofill_sessions(session_id);
