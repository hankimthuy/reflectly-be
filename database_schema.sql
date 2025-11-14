-- =====================================================
-- Reflectly Database Schema for Neon PostgreSQL
-- =====================================================

-- Drop existing tables if they exist (in correct order due to foreign keys)
DROP TABLE IF EXISTS entry_emotions CASCADE;
DROP TABLE IF EXISTS entries CASCADE;
DROP TABLE IF EXISTS factors CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- =====================================================
-- 1. USERS TABLE
-- =====================================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    full_name VARCHAR(255),
    picture_url VARCHAR(500) NOT NULL
);

-- Index for users table
CREATE INDEX idx_users_email ON users(email);

-- =====================================================
-- 2. FACTORS TABLE
-- =====================================================
CREATE TABLE factors (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 3. ENTRIES TABLE
-- =====================================================
CREATE TABLE entries (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    title VARCHAR(100) NOT NULL,
    reflection TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for entries table
CREATE INDEX idx_user_entries ON entries(user_id);
CREATE INDEX idx_entries_created_at ON entries(created_at);

-- =====================================================
-- 4. ENTRY_EMOTIONS TABLE (ElementCollection)
-- =====================================================
CREATE TABLE entry_emotions (
    entry_id VARCHAR(36) NOT NULL,
    emotion VARCHAR(50) NOT NULL,
    PRIMARY KEY (entry_id, emotion),
    FOREIGN KEY (entry_id) REFERENCES entries(id) ON DELETE CASCADE
);

-- =====================================================
-- 5. SAMPLE DATA (Optional - for testing)
-- =====================================================

-- Insert sample users
INSERT INTO users (email, full_name, picture_url) VALUES 
('test@example.com', 'Test User', 'https://example.com/avatar.jpg'),
('admin@example.com', 'Admin User', 'https://example.com/admin.jpg');

-- Insert sample factors
INSERT INTO factors (name) VALUES 
('Work'),
('Family'),
('Health'),
('Hobbies'),
('Travel'),
('Learning');

-- Insert sample entries
INSERT INTO entries (id, user_id, title, reflection, created_at, updated_at) VALUES 
('550e8400-e29b-41d4-a716-446655440001', 'test-user-1', 'Great day at work', 'I had a productive day and learned new things about Spring Boot.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440002', 'test-user-1', 'Family time', 'Spent quality time with family today. Feeling grateful.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440003', 'test-user-2', 'Morning workout', 'Completed my morning run and feeling energized.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample emotions for entries
INSERT INTO entry_emotions (entry_id, emotion) VALUES 
('550e8400-e29b-41d4-a716-446655440001', 'happy'),
('550e8400-e29b-41d4-a716-446655440001', 'productive'),
('550e8400-e29b-41d4-a716-446655440001', 'confident'),
('550e8400-e29b-41d4-a716-446655440002', 'grateful'),
('550e8400-e29b-41d4-a716-446655440002', 'happy'),
('550e8400-e29b-41d4-a716-446655440002', 'blessed'),
('550e8400-e29b-41d4-a716-446655440003', 'energized'),
('550e8400-e29b-41d4-a716-446655440003', 'accomplished'),
('550e8400-e29b-41d4-a716-446655440003', 'motivated');

-- =====================================================
-- 6. VERIFY TABLES CREATED
-- =====================================================
-- Run these queries to verify the schema was created correctly:

-- SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';
-- SELECT * FROM users;
-- SELECT * FROM factors;
-- SELECT * FROM entries;
-- SELECT * FROM entry_emotions;
