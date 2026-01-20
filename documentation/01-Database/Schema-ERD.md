# Database Schema & ERD

## Entity Relationship Diagram

```
┌─────────────────────┐
│        User         │
├─────────────────────┤
│ id (PK)             │
│ email               │
│ name                │
│ core_values         │
│ created_at          │
│ updated_at          │
└─────────┬───────────┘
          │
          │ 1:N
          ▼
┌─────────────────────┐      ┌─────────────────────┐
│     EnergyLog       │      │  SocialConnection   │
├─────────────────────┤      ├─────────────────────┤
│ id (PK)             │      │ id (PK)             │
│ user_id (FK)        │◄────►│ user_id (FK)        │
│ level               │      │ friend_name         │
│ context_tag         │      │ orbit_distance      │
│ notes               │      │ impact_score        │
│ timestamp           │      │ last_interaction    │
│ created_at          │      │ created_at          │
└─────────────────────┘      │ updated_at          │
                             └─────────────────────┘

┌─────────────────────┐
│   ActionProtocol    │
├─────────────────────┤
│ id (PK)             │
│ user_id (FK)        │
│ trigger             │
│ script_template     │
│ usage_count         │
│ created_at          │
│ updated_at          │
└─────────────────────┘
```

## Entity Definitions

### User
The central entity representing an authenticated user of MimoSe.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK | Unique identifier |
| `email` | VARCHAR(255) | UNIQUE, NOT NULL | User's email address |
| `name` | VARCHAR(100) | NOT NULL | Display name |
| `core_values` | TEXT[] / JSON | NULLABLE | Array of user's defined core values |
| `created_at` | TIMESTAMP | NOT NULL | Account creation timestamp |
| `updated_at` | TIMESTAMP | NOT NULL | Last update timestamp |

### EnergyLog
Tracks user's energy levels over time with contextual information.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK | Unique identifier |
| `user_id` | UUID | FK → User.id, NOT NULL | Reference to user |
| `level` | INTEGER | NOT NULL, CHECK(1-10) | Energy level (1=lowest, 10=highest) |
| `context_tag` | VARCHAR(50) | NOT NULL | Situational context (work, social, rest, etc.) |
| `notes` | TEXT | NULLABLE | Optional user notes |
| `timestamp` | TIMESTAMP | NOT NULL | When the energy was recorded |
| `created_at` | TIMESTAMP | NOT NULL | Record creation timestamp |

### SocialConnection
Maps the user's relationships and their impact.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK | Unique identifier |
| `user_id` | UUID | FK → User.id, NOT NULL | Reference to user |
| `friend_name` | VARCHAR(100) | NOT NULL | Name of the connection |
| `orbit_distance` | INTEGER | NOT NULL, CHECK(1-5) | Closeness level (1=closest, 5=farthest) |
| `impact_score` | DECIMAL(3,2) | NULLABLE | Energy impact (-1.00 to +1.00) |
| `last_interaction` | TIMESTAMP | NULLABLE | Last recorded interaction |
| `created_at` | TIMESTAMP | NOT NULL | Record creation timestamp |
| `updated_at` | TIMESTAMP | NOT NULL | Last update timestamp |

### ActionProtocol
Stores pre-defined response scripts for triggers.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK | Unique identifier |
| `user_id` | UUID | FK → User.id, NOT NULL | Reference to user |
| `trigger` | VARCHAR(255) | NOT NULL | Trigger condition description |
| `script_template` | TEXT | NOT NULL | Response script content |
| `usage_count` | INTEGER | DEFAULT 0 | Number of times used |
| `created_at` | TIMESTAMP | NOT NULL | Record creation timestamp |
| `updated_at` | TIMESTAMP | NOT NULL | Last update timestamp |

## Indexes

```sql
-- User lookups
CREATE INDEX idx_user_email ON users(email);

-- EnergyLog queries
CREATE INDEX idx_energylog_user_timestamp ON energy_logs(user_id, timestamp DESC);
CREATE INDEX idx_energylog_context ON energy_logs(context_tag);

-- SocialConnection queries
CREATE INDEX idx_socialconnection_user ON social_connections(user_id);
CREATE INDEX idx_socialconnection_orbit ON social_connections(user_id, orbit_distance);

-- ActionProtocol queries
CREATE INDEX idx_actionprotocol_user ON action_protocols(user_id);
```

## Relationships Summary

| Parent | Child | Relationship | Cascade |
|--------|-------|--------------|---------|
| User | EnergyLog | One-to-Many | DELETE |
| User | SocialConnection | One-to-Many | DELETE |
| User | ActionProtocol | One-to-Many | DELETE |
