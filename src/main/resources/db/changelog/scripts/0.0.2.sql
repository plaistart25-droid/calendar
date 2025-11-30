--liquibase formatted sql

--changeset DanielK:2

CREATE TABLE user_messages_log (
                              id           BIGSERIAL PRIMARY KEY,
                              telegram_id  BIGINT,
                              username     TEXT,
                              firstname     TEXT,
                              lastname     TEXT,
                              google_email TEXT,
                              message      TEXT,
                              created_at   TIMESTAMP
                                  DEFAULT NOW()
);

CREATE INDEX idx_user_messages_log_created_at
    ON user_messages_log (created_at);

CREATE INDEX idx_user_messages_log_telegram_id
    ON user_messages_log (telegram_id);
