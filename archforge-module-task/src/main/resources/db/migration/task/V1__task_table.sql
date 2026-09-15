-- task/V1: task module table. The module owns its schema (previously hbm2ddl.auto=update
-- under the detached example). IF NOT EXISTS keeps dev databases that already have
-- the hbm2ddl-created table working; fresh databases get the Flyway-managed DDL.
CREATE TABLE IF NOT EXISTS task (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255),
    description VARCHAR(255),
    uid BIGINT NOT NULL,
    status VARCHAR(255),
    creator_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updater_id BIGINT,
    update_time TIMESTAMP,
    deleted INT DEFAULT 0 NOT NULL
);
