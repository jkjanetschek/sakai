-- Cleanup stale locks in sakai_locks

truncate table sakai_locks;

INSERT INTO mci_schema_migrations (name, executed_at)
VALUES ('sakai_25_1_mci_05.sql', NOW(6));