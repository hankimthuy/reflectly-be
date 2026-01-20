# Data Dictionary

## Enums & Constrained Values

### Context Tags (EnergyLog.context_tag)
Predefined categories for energy log context.

| Value | Description |
|-------|-------------|
| `work` | Work-related activities |
| `social` | Social interactions and gatherings |
| `rest` | Rest, relaxation, and recovery time |
| `exercise` | Physical activity and sports |
| `creative` | Creative endeavors and hobbies |
| `learning` | Study and learning activities |
| `family` | Family-related interactions |
| `alone` | Solo time and personal reflection |

> **Note**: Users may define custom tags beyond these defaults.

### Orbit Distance (SocialConnection.orbit_distance)
Represents relationship closeness using orbital metaphor.

| Value | Name | Description |
|-------|------|-------------|
| `1` | Inner Core | Closest relationships (partner, best friends, immediate family) |
| `2` | Close Orbit | Close friends and trusted colleagues |
| `3` | Mid Orbit | Regular friends and coworkers |
| `4` | Outer Orbit | Acquaintances and occasional contacts |
| `5` | Distant Orbit | Peripheral connections, rarely interact |

### Energy Level (EnergyLog.level)
Scale for measuring personal energy.

| Value | Label | Description |
|-------|-------|-------------|
| `1-2` | Very Low | Exhausted, depleted, need immediate rest |
| `3-4` | Low | Tired, struggling to focus |
| `5-6` | Moderate | Functional, average energy |
| `7-8` | High | Energized, productive, engaged |
| `9-10` | Very High | Peak energy, flow state |

### Impact Score (SocialConnection.impact_score)
Measures how a relationship affects the user's energy.

| Range | Interpretation |
|-------|----------------|
| `-1.00 to -0.50` | Strongly draining relationship |
| `-0.49 to -0.10` | Mildly draining relationship |
| `-0.09 to +0.09` | Neutral impact |
| `+0.10 to +0.49` | Mildly energizing relationship |
| `+0.50 to +1.00` | Strongly energizing relationship |

## Field Constraints

### Validation Rules

| Entity | Field | Constraint |
|--------|-------|------------|
| User | email | Valid email format, max 255 chars |
| User | name | 1-100 characters, non-empty |
| User | core_values | Max 10 values, each max 50 chars |
| EnergyLog | level | Integer between 1 and 10 inclusive |
| EnergyLog | context_tag | 1-50 characters |
| EnergyLog | notes | Max 1000 characters |
| SocialConnection | friend_name | 1-100 characters, non-empty |
| SocialConnection | orbit_distance | Integer between 1 and 5 inclusive |
| SocialConnection | impact_score | Decimal between -1.00 and +1.00 |
| ActionProtocol | trigger | 1-255 characters |
| ActionProtocol | script_template | Max 5000 characters |

### Default Values

| Entity | Field | Default |
|--------|-------|---------|
| EnergyLog | timestamp | CURRENT_TIMESTAMP |
| SocialConnection | orbit_distance | 3 (Mid Orbit) |
| SocialConnection | impact_score | 0.00 (Neutral) |
| ActionProtocol | usage_count | 0 |

## Data Types Reference

### PostgreSQL Mappings

| Application Type | PostgreSQL Type | JPA Annotation |
|------------------|-----------------|----------------|
| UUID | uuid | `@GeneratedValue(strategy = GenerationType.UUID)` |
| String | VARCHAR(n) | `@Column(length = n)` |
| Text | TEXT | `@Column(columnDefinition = "TEXT")` |
| Integer | INTEGER | `@Column` |
| Decimal | DECIMAL(p,s) | `@Column(precision = p, scale = s)` |
| Timestamp | TIMESTAMP | `@Column` with `LocalDateTime` |
| Array | TEXT[] or JSONB | `@Type(type = "list-array")` or JSON converter |

## Audit Fields

All entities include standard audit fields:

| Field | Type | Description |
|-------|------|-------------|
| `created_at` | TIMESTAMP | Auto-set on insert (non-updatable) |
| `updated_at` | TIMESTAMP | Auto-updated on every modification |

### JPA Implementation
```java
@CreatedDate
@Column(nullable = false, updatable = false)
private LocalDateTime createdAt;

@LastModifiedDate
@Column(nullable = false)
private LocalDateTime updatedAt;
```
