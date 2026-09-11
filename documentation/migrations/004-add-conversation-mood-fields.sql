-- 004 — Aura Soft redesign: server-side mood readings for Coach chat sessions.
-- Additive, nullable columns, online-safe on PostgreSQL.
-- Run against the production DB BEFORE deploying the jar that adds
-- ConversationMessageEntity.moodEmotion/moodScore and ConversationEntity.initial/finalMood*
-- (prod runs ddl-auto: validate and will fail boot otherwise).

ALTER TABLE conversation_messages ADD COLUMN IF NOT EXISTS mood_emotion varchar(20);
ALTER TABLE conversation_messages ADD COLUMN IF NOT EXISTS mood_score double precision;

ALTER TABLE conversations ADD COLUMN IF NOT EXISTS initial_mood_emotion varchar(20);
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS initial_mood_score double precision;
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS final_mood_emotion varchar(20);
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS final_mood_score double precision;
