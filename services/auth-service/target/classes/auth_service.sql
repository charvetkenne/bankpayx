-- -- =========================================================
-- --  DATABASE: bankpayx_auth
-- --  SERVICE: auth-service
-- --  DESCRIPTION: Tables des utilisateurs + rôles
-- -- =========================================================

-- -- ---------------------------
-- --  SCHEMA
-- -- ---------------------------
-- CREATE SCHEMA IF NOT EXISTS auth_service;
-- SET search_path TO auth_service;

-- -- ---------------------------
-- --  TABLE roles
-- -- ---------------------------
-- CREATE TABLE IF NOT EXISTS roles (
--     id SERIAL PRIMARY KEY,
--     name VARCHAR(50) UNIQUE NOT NULL
-- );

-- -- valeurs par défaut
-- INSERT INTO roles (name) VALUES
--     ('ROLE_ADMIN'),
--     ('ROLE_USER')
--     ON CONFLICT DO NOTHING;

-- -- ---------------------------
-- --  TABLE users
-- -- ---------------------------
-- CREATE TABLE IF NOT EXISTS users (
--     id UUID PRIMARY KEY,
--     username VARCHAR(150) UNIQUE NOT NULL,
--     email VARCHAR(255) UNIQUE NOT NULL,
--     password_hash VARCHAR(255) NOT NULL,
--     role_id INTEGER NOT NULL REFERENCES roles(id),
--     created_at TIMESTAMP NOT NULL DEFAULT NOW(),
--     updated_at TIMESTAMP NOT NULL DEFAULT NOW()
-- );

-- -- Index recommandés
-- CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
-- CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- -- Trigger pour updated_at
-- CREATE OR REPLACE FUNCTION update_timestamp()
-- RETURNS TRIGGER AS $$
-- BEGIN
--   NEW.updated_at = NOW();
--   RETURN NEW;
-- END;
-- $$ LANGUAGE plpgsql;

-- CREATE TRIGGER trg_update_timestamp
-- BEFORE UPDATE ON users
-- FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- -- ---------------------------
-- --  TABLE refresh_tokens (optionnel, si Refresh Tokens)
-- -- ---------------------------
-- CREATE TABLE IF NOT EXISTS refresh_tokens (
--     id UUID PRIMARY KEY,
--     user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
--     token TEXT NOT NULL,
--     expires_at TIMESTAMP NOT NULL,
--     created_at TIMESTAMP NOT NULL DEFAULT NOW()
-- );

-- CREATE INDEX IF NOT EXISTS idx_refresh_user ON refresh_tokens(user_id);
-- CREATE INDEX IF NOT EXISTS idx_refresh_expires ON refresh_tokens(expires_at);
