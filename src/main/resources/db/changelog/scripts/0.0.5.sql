--liquibase formatted sql

--changeset DanielK:4

CREATE TABLE user_auth_notification (
                                        id           BIGSERIAL PRIMARY KEY,
                                        telegram_id  BIGINT NOT NULL,
                                        status       TEXT NOT NULL,
                                        text         TEXT,
                                        execute_at   TIMESTAMP NOT NULL
);

CREATE INDEX idx_user_auth_notification_status_execute_at
    ON user_auth_notification (status, execute_at);