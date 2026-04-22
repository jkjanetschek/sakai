/* create minimal table to track DDL-conversion executions */
CREATE TABLE mci_schema_migrations (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    executed_at DATETIME(6) NOT NULL,
    notes TEXT NULL
);


INSERT INTO mci_schema_migrations (name, executed_at)
VALUES ('sakai_00_mci_01.sql', NOW(6));






