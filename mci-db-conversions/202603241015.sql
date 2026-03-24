/* SAKAIME-866 */
ALTER TABLE lesson_builder_ch_status
ADD COLUMN checked_at DATETIME(6);

INSERT INTO mci_schema_migrations (id, executed_at)
VALUES ('202603241015.sql', NOW(6));