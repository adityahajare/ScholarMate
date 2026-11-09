-- ScholarMate Advanced ML Architecture: Search & Feedback Schema Migration
-- 1. Enable pg_trgm extension for fuzzy trigram search
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 2. GIN trigram indexes for fast typo-tolerant search
CREATE INDEX IF NOT EXISTS idx_scholarships_name_trgm ON scholarships USING GIN (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_scholarships_desc_trgm ON scholarships USING GIN (description gin_trgm_ops);

-- 3. Student recommendation feedback tracking table
CREATE TABLE IF NOT EXISTS recommendation_feedback (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    scholarship_id BIGINT NOT NULL REFERENCES scholarships(id) ON DELETE CASCADE,
    event_type VARCHAR(40) NOT NULL, -- CLICK, SAVE, DISMISS, APPLY_START, SUBMIT, AWARD_CONFIRMED
    event_timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    recommendation_version VARCHAR(100),
    model_version VARCHAR(100),
    processed_for_training BOOLEAN NOT NULL DEFAULT FALSE,
    context_data JSONB
);

CREATE INDEX IF NOT EXISTS idx_rec_feedback_student ON recommendation_feedback(student_id);
CREATE INDEX IF NOT EXISTS idx_rec_feedback_scholarship ON recommendation_feedback(scholarship_id);
CREATE INDEX IF NOT EXISTS idx_rec_feedback_event ON recommendation_feedback(event_type);
CREATE INDEX IF NOT EXISTS idx_rec_feedback_unprocessed ON recommendation_feedback(processed_for_training) WHERE processed_for_training = FALSE;
