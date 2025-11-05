--liquibase formatted sql

--changeset DanielK:1


CREATE TABLE IF NOT EXISTS telegram_users (
                                              id BIGSERIAL PRIMARY KEY,
                                              telegram_id BIGINT NOT NULL,
                                              username TEXT,
                                              firstname TEXT,
                                              lastname TEXT,
                                              language_code TEXT,
                                              response_count   BIGINT DEFAULT 0,
                                              updated TIMESTAMP,
                                              created TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE INDEX IF NOT EXISTS ix_telegram_users__tg
    ON telegram_users (telegram_id);

CREATE TABLE IF NOT EXISTS user_google_calendar (
                                                    telegram_id BIGINT NOT NULL PRIMARY KEY,
                                                    calendar_id TEXT
);

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS assistant_google_oauth (
                                                      telegram_id          BIGINT       PRIMARY KEY,           -- ключ = telegramId
                                                      google_sub           VARCHAR(128),
    email                VARCHAR(320),
    refresh_token_enc    TEXT,                               -- шифротекст AES-GCM
    access_token         TEXT,
    access_expires_at    TIMESTAMP WITH TIME ZONE,           -- лучше хранить в UTC
    scope                TEXT,
    default_calendar_id  TEXT,
    last_refresh_at      TIMESTAMP WITH TIME ZONE,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
    );

-- По желанию: ускорить выборки по email/sub (диагностика/отчёты)
CREATE INDEX IF NOT EXISTS idx_assist_oauth_email      ON assistant_google_oauth (email);
CREATE INDEX IF NOT EXISTS idx_assist_oauth_google_sub ON assistant_google_oauth (google_sub);

CREATE TABLE IF NOT EXISTS oauth_link (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),  -- публичный непредсказуемый ID
    telegram_id     BIGINT      NOT NULL,                        -- к какому чату относится
    expire_at   TIMESTAMP WITH TIME ZONE NOT NULL,           -- TTL
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
    );

-- Быстрая очистка просроченных
CREATE INDEX IF NOT EXISTS idx_oauth_link_expire ON oauth_link (expire_at);

CREATE TABLE IF NOT EXISTS oauth_state (
    state       UUID PRIMARY KEY DEFAULT gen_random_uuid(),  -- этот UUID кладёшь в &state=...
    telegram_id     BIGINT      NOT NULL,
    verifier    VARCHAR(256) NOT NULL,                       -- PKCE code_verifier (хранится временно)
    expire_at   TIMESTAMP WITH TIME ZONE NOT NULL,           -- TTL
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_oauth_state_expire ON oauth_state (expire_at);

CREATE TABLE notified_event (
                                id BIGSERIAL PRIMARY KEY,
                                calendar_id TEXT NOT NULL,
                                event_id TEXT NOT NULL,
                                user_id BIGINT NOT NULL,
                                notified_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
                                CONSTRAINT uq_calendar_event UNIQUE (calendar_id, event_id, user_id)
);
CREATE INDEX idx_notified_at ON notified_event(notified_at);

CREATE TABLE user_google_calendar_cache (
                                            id BIGSERIAL PRIMARY KEY,
                                            telegram_id BIGINT NOT NULL,
                                            calendar_id TEXT NOT NULL,
                                            summary TEXT NOT NULL
);

CREATE TABLE ai_message_log (
                                id BIGSERIAL PRIMARY KEY,
                                request TEXT,
                                response TEXT
);