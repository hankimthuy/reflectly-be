# Manual DB migrations (interim, pre-Flyway)

This project has **no migration tool yet**. Locally, Hibernate `ddl-auto: update`
(see `application.yml` / `application-local.yml`) auto-applies additive schema
changes on boot, so nothing needs to be run by hand on a dev machine.

**Production uses `ddl-auto: validate`** (`application-prod.yml`): the app will
**fail to boot** if an entity references a column the database does not have.
Therefore, for every schema change, the SQL in this folder must be run against
the production database **before** deploying the jar that introduces it.

All statements here are additive and online-safe on PostgreSQL (nullable column
adds, new tables) — old rows remain valid.

## Follow-up
Adopt Flyway with a baseline migration as a dedicated task **before the next
schema change**. Several features (Energy, Entry Templates, Action Protocol,
Dashboard) are growing the schema with no migration history; hand-running ALTERs
against prod is a stopgap, not the plan.

## Ordered statements
| # | File | Introduced by | Notes |
|---|------|---------------|-------|
| 001 | `001-add-entry-template-key.sql` | Entry Templates | Nullable column on `entries` |
