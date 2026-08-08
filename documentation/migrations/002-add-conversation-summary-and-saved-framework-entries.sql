-- 002 — Aura Insight Catcher: conversation summaries + saved framework entries.
-- Additive, nullable/new-table, online-safe on PostgreSQL.
-- Run against the production DB BEFORE deploying the jar that adds
-- ConversationEntity.summary and SavedFrameworkEntryEntity (prod runs
-- ddl-auto: validate and will fail boot otherwise).

ALTER TABLE conversations ADD COLUMN IF NOT EXISTS summary TEXT;

CREATE TABLE IF NOT EXISTS saved_framework_entries (
    id                  varchar(255) PRIMARY KEY,
    user_id             bigint NOT NULL REFERENCES users(id),
    conversation_id     varchar(255) REFERENCES conversations(id),
    framework_type      varchar(20) NOT NULL,
    title               varchar(200),
    payload             jsonb NOT NULL,
    created_date        timestamp(6),
    created_by          varchar(255),
    last_modified_date  timestamp(6),
    last_modified_by    varchar(255)
);

CREATE INDEX IF NOT EXISTS idx_saved_framework_entries_user ON saved_framework_entries (user_id);
