--liquibase formatted sql

--changeset DanielK:3

CREATE TABLE user_notification_settings (
                                            id                  BIGSERIAL PRIMARY KEY,
                                            telegram_id         BIGINT    NOT NULL,
                                            daily_enabled       BOOLEAN   NOT NULL DEFAULT TRUE,
                                            daily_time          TIME,
                                            utc_offset_hours    INTEGER,
                                            last_daily_notified DATE
);

CREATE UNIQUE INDEX ux_user_notification_settings_telegram
    ON user_notification_settings (telegram_id);

CREATE INDEX idx_user_notification_settings_daily_enabled
    ON user_notification_settings (daily_enabled);