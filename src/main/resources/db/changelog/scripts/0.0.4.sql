--liquibase formatted sql

--changeset DanielK:4

ALTER TABLE notified_event
    ADD COLUMN reminder_minutes INT;