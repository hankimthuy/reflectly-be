-- 001 — Entry Templates: track which guided template an entry was written with.
-- Additive, nullable, online-safe on PostgreSQL. Existing rows keep template_key = NULL.
-- Run against the production DB BEFORE deploying the jar that adds EntryEntity.templateKey
-- (prod runs ddl-auto: validate and will fail boot otherwise).

ALTER TABLE entries ADD COLUMN IF NOT EXISTS template_key varchar(50);
