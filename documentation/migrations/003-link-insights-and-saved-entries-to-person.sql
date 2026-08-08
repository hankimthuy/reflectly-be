-- 003 — Phase 2 (ACT Matrix / SWOT / Life Positions / PRM / Insight↔Person linking).
-- Additive, nullable columns, online-safe on PostgreSQL.
-- Run against the production DB BEFORE deploying the jar that adds InsightEntity.person and
-- SavedFrameworkEntryEntity.person (prod runs ddl-auto: validate and will fail boot otherwise).

ALTER TABLE insights ADD COLUMN IF NOT EXISTS person_id varchar(255) REFERENCES people(id);
CREATE INDEX IF NOT EXISTS idx_insights_person ON insights (person_id);

ALTER TABLE saved_framework_entries ADD COLUMN IF NOT EXISTS person_id varchar(255) REFERENCES people(id);
CREATE INDEX IF NOT EXISTS idx_saved_framework_entries_person ON saved_framework_entries (person_id);
