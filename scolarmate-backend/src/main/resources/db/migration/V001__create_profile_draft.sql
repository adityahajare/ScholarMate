CREATE TABLE IF NOT EXISTS profile_draft (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    data JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_profile_draft_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Trigger to update updated_at on row modification
CREATE OR REPLACE FUNCTION update_profile_draft_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_profile_draft_update ON profile_draft;
CREATE TRIGGER trg_profile_draft_update
BEFORE UPDATE ON profile_draft
FOR EACH ROW EXECUTE FUNCTION update_profile_draft_timestamp();

