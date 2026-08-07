-- =====================================================================
-- Aura Self AI (Reflectly) — SCHEMA KHỞI TẠO CHO DATABASE MỚI (Supabase)
--
-- Dùng cho DB TRỐNG hoàn toàn. Không phải migration.
-- Sinh trực tiếp từ 10 @Entity + 2 @CollectionTable trong code hiện tại.
--
-- CÁCH CHẠY:
--   Supabase Dashboard -> SQL Editor -> New query -> dán toàn bộ -> Run.
--
-- Sau khi chạy xong, backend chạy với ddl-auto: validate sẽ start được.
-- =====================================================================

-- ---------------------------------------------------------------------
-- QUY ƯỚC KIỂU DỮ LIỆU (đừng đổi, nếu sai validate sẽ fail)
--   Instant (Java)            -> TIMESTAMPTZ   (Hibernate 6 map như vậy)
--   @Id String (không @Column)-> VARCHAR(255)  (mặc định của Hibernate)
--   UserEntity.id là Long     -> BIGSERIAL / BIGINT cho FK
--   Một số entity dùng "String userId" (không FK) -> VARCHAR(36)
-- ---------------------------------------------------------------------


-- =====================================================================
-- 1. users
-- =====================================================================
CREATE TABLE IF NOT EXISTS users (
    id                   BIGSERIAL    PRIMARY KEY,
    email                VARCHAR(254) UNIQUE,
    username             VARCHAR(50)  UNIQUE,
    full_name            VARCHAR(255),
    picture_url          VARCHAR(255) NOT NULL,
    password_hash        VARCHAR(255),
    onboarding_completed BOOLEAN      NOT NULL DEFAULT FALSE
);

-- @ElementCollection(fetch = EAGER) List<String> coreValues
CREATE TABLE IF NOT EXISTS user_core_values (
    user_id BIGINT NOT NULL,
    value   VARCHAR(50),
    CONSTRAINT fk_user_core_values_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_user_core_values_user ON user_core_values (user_id);


-- =====================================================================
-- 2. entries — nhật ký
-- =====================================================================
CREATE TABLE IF NOT EXISTS entries (
    id                 VARCHAR(255) PRIMARY KEY,
    user_id            VARCHAR(36)  NOT NULL,
    title              VARCHAR(100) NOT NULL,
    reflection         TEXT         NOT NULL,
    template_key       VARCHAR(50),
    created_date       TIMESTAMPTZ,
    created_by         VARCHAR(255),
    last_modified_date TIMESTAMPTZ,
    last_modified_by   VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS idx_user_entries        ON entries (user_id);
CREATE INDEX IF NOT EXISTS idx_entries_created_date ON entries (created_date);

-- @ElementCollection List<String> emotions
-- Cố ý KHÔNG đặt PRIMARY KEY ghép: entity khai List (cho phép trùng),
-- PK ghép sẽ chặn insert nếu user chọn trùng cảm xúc.
CREATE TABLE IF NOT EXISTS entry_emotions (
    entry_id VARCHAR(255) NOT NULL,
    emotion  VARCHAR(50),
    CONSTRAINT fk_entry_emotions_entry
        FOREIGN KEY (entry_id) REFERENCES entries (id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_entry_emotions_entry ON entry_emotions (entry_id);


-- =====================================================================
-- 3. conversations — phiên chat với AI Coach
-- =====================================================================
CREATE TABLE IF NOT EXISTS conversations (
    id                 VARCHAR(255) PRIMARY KEY,
    user_id            BIGINT       NOT NULL,
    status             VARCHAR(20)  NOT NULL,  -- ACTIVE|ENDED|EXTRACTING|EXTRACTED|EXTRACTION_FAILED
    started_at         TIMESTAMPTZ  NOT NULL,
    ended_at           TIMESTAMPTZ,
    created_date       TIMESTAMPTZ,
    created_by         VARCHAR(255),
    last_modified_date TIMESTAMPTZ,
    last_modified_by   VARCHAR(255),
    CONSTRAINT fk_conversations_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_conversations_user ON conversations (user_id);


-- =====================================================================
-- 4. conversation_messages
--    content được mã hoá AES-256 ở tầng app (EncryptedStringConverter)
--    -> luôn dài hơn plaintext, phải để TEXT.
--    -> BẮT BUỘC có app setting ENCRYPTION_KEY, nếu không sẽ lỗi runtime.
-- =====================================================================
CREATE TABLE IF NOT EXISTS conversation_messages (
    id                 VARCHAR(255) PRIMARY KEY,
    conversation_id    VARCHAR(255) NOT NULL,
    role               VARCHAR(20)  NOT NULL,  -- USER | ASSISTANT
    content            TEXT,
    created_date       TIMESTAMPTZ,
    created_by         VARCHAR(255),
    last_modified_date TIMESTAMPTZ,
    last_modified_by   VARCHAR(255),
    CONSTRAINT fk_conversation_messages_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations (id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_conversation_messages_conversation
    ON conversation_messages (conversation_id);


-- =====================================================================
-- 5. insights
-- =====================================================================
CREATE TABLE IF NOT EXISTS insights (
    id                 VARCHAR(255) PRIMARY KEY,
    user_id            BIGINT       NOT NULL,
    conversation_id    VARCHAR(255),
    insight_text       TEXT         NOT NULL,
    category           VARCHAR(20)  NOT NULL,  -- VALUE | BEHAVIOR_PATTERN | RELATIONSHIP
    created_date       TIMESTAMPTZ,
    created_by         VARCHAR(255),
    last_modified_date TIMESTAMPTZ,
    last_modified_by   VARCHAR(255),
    CONSTRAINT fk_insights_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_insights_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations (id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_insights_user ON insights (user_id);


-- =====================================================================
-- 6. people — bản đồ mối quan hệ
-- =====================================================================
CREATE TABLE IF NOT EXISTS people (
    id                 VARCHAR(255) PRIMARY KEY,
    user_id            BIGINT       NOT NULL,
    name               VARCHAR(100) NOT NULL,
    relationship_type  VARCHAR(20)  NOT NULL,  -- FAMILY|FRIEND|PARTNER|COLLEAGUE|MANAGER|OTHER
    notes              TEXT,
    last_mentioned_at  TIMESTAMPTZ,
    created_date       TIMESTAMPTZ,
    created_by         VARCHAR(255),
    last_modified_date TIMESTAMPTZ,
    last_modified_by   VARCHAR(255),
    CONSTRAINT fk_people_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_people_user ON people (user_id);


-- =====================================================================
-- 7. relationship_events
-- =====================================================================
CREATE TABLE IF NOT EXISTS relationship_events (
    id                 VARCHAR(255) PRIMARY KEY,
    person_id          VARCHAR(255) NOT NULL,
    conversation_id    VARCHAR(255),
    event_type         VARCHAR(20)  NOT NULL,  -- CONFLICT | BONDING | NEUTRAL
    summary            TEXT         NOT NULL,
    sentiment_score    DOUBLE PRECISION,
    created_date       TIMESTAMPTZ,
    created_by         VARCHAR(255),
    last_modified_date TIMESTAMPTZ,
    last_modified_by   VARCHAR(255),
    CONSTRAINT fk_relationship_events_person
        FOREIGN KEY (person_id) REFERENCES people (id) ON DELETE CASCADE,
    CONSTRAINT fk_relationship_events_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations (id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_relationship_events_person
    ON relationship_events (person_id);


-- =====================================================================
-- 8. action_protocols
--    Lưu ý: entity dùng String userId (VARCHAR 36), KHÔNG phải FK.
-- =====================================================================
CREATE TABLE IF NOT EXISTS action_protocols (
    id                 VARCHAR(255) PRIMARY KEY,
    user_id            VARCHAR(36)  NOT NULL,
    title              VARCHAR(255) NOT NULL,
    trigger_situation  VARCHAR(255) NOT NULL,
    script             TEXT         NOT NULL,
    usage_count        INTEGER      NOT NULL DEFAULT 0,
    last_used_at       TIMESTAMPTZ,
    created_date       TIMESTAMPTZ,
    created_by         VARCHAR(255),
    last_modified_date TIMESTAMPTZ,
    last_modified_by   VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS idx_actionprotocol_user ON action_protocols (user_id);


-- =====================================================================
-- 9. protocol_usages
-- =====================================================================
CREATE TABLE IF NOT EXISTS protocol_usages (
    id                 VARCHAR(255) PRIMARY KEY,
    protocol_id        VARCHAR(36)  NOT NULL,
    user_id            VARCHAR(36)  NOT NULL,
    effectiveness      VARCHAR(20)  NOT NULL,  -- WORKED | PARTIAL | DIDNT_WORK
    note               VARCHAR(500),
    used_at            TIMESTAMPTZ  NOT NULL,
    created_date       TIMESTAMPTZ,
    created_by         VARCHAR(255),
    last_modified_date TIMESTAMPTZ,
    last_modified_by   VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS idx_protocolusage_protocol ON protocol_usages (protocol_id);
CREATE INDEX IF NOT EXISTS idx_protocolusage_user     ON protocol_usages (user_id);


-- =====================================================================
-- 10. energy_logs
-- =====================================================================
CREATE TABLE IF NOT EXISTS energy_logs (
    id                 VARCHAR(255) PRIMARY KEY,
    user_id            VARCHAR(36)  NOT NULL,
    level              INTEGER      NOT NULL,
    context_tag        VARCHAR(50)  NOT NULL,
    notes              TEXT,
    logged_at          TIMESTAMPTZ  NOT NULL,
    created_date       TIMESTAMPTZ,
    created_by         VARCHAR(255),
    last_modified_date TIMESTAMPTZ,
    last_modified_by   VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS idx_energylog_user_loggedat ON energy_logs (user_id, logged_at);


-- =====================================================================
-- 11. BẢO MẬT — BẮT BUỘC LÀM TRÊN SUPABASE
--
-- Supabase tự expose mọi bảng trong schema "public" ra REST API công khai
-- (PostgREST) cho anon key. App này KHÔNG dùng REST API của Supabase —
-- backend Spring Boot nối thẳng bằng JDBC. Vì vậy bật RLS mà không tạo
-- policy nào = chặn sạch mọi truy cập qua REST, trong khi role "postgres"
-- mà Spring dùng vẫn bypass RLS nên app chạy bình thường.
--
-- Đây là nhật ký cá nhân — không bật RLS là rò dữ liệu ra internet.
-- =====================================================================
ALTER TABLE users                 ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_core_values      ENABLE ROW LEVEL SECURITY;
ALTER TABLE entries               ENABLE ROW LEVEL SECURITY;
ALTER TABLE entry_emotions        ENABLE ROW LEVEL SECURITY;
ALTER TABLE conversations         ENABLE ROW LEVEL SECURITY;
ALTER TABLE conversation_messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE insights              ENABLE ROW LEVEL SECURITY;
ALTER TABLE people                ENABLE ROW LEVEL SECURITY;
ALTER TABLE relationship_events   ENABLE ROW LEVEL SECURITY;
ALTER TABLE action_protocols      ENABLE ROW LEVEL SECURITY;
ALTER TABLE protocol_usages       ENABLE ROW LEVEL SECURITY;
ALTER TABLE energy_logs           ENABLE ROW LEVEL SECURITY;


-- =====================================================================
-- 12. KIỂM TRA
-- =====================================================================
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;

-- Phải ra đúng 12 dòng:
--   action_protocols, conversation_messages, conversations, energy_logs,
--   entries, entry_emotions, insights, people, protocol_usages,
--   relationship_events, user_core_values, users
