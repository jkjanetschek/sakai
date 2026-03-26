/* create minimal table to track DDL-conversion executions */
CREATE TABLE mci_schema_migrations (
    id VARCHAR(100) PRIMARY KEY,
    executed_at DATETIME(6) NOT NULL
);

INSERT INTO mci_schema_migrations (id, executed_at)
VALUES ('202603241014.sql', NOW(6));