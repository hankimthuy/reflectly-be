package org.mentorship.reflectly.model;

/**
 * Which structured "insight catcher" template a {@link SavedFrameworkEntryEntity} was captured
 * with. Only FREEFORM and JOHARI_WINDOW are reachable via the API/UI today (Phase 1) — the rest
 * are reserved so a later phase can add them without a schema change.
 */
public enum FrameworkType {
    FREEFORM,
    JOHARI_WINDOW,
    ACT_MATRIX,
    PERSONAL_SWOT,
    LIFE_POSITIONS
}
