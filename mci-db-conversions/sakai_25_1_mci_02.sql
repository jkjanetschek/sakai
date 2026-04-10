-- already done in mci sakai 23 by snr

/* SAKAIME-866 */
ALTER TABLE lesson_builder_ch_status
ADD COLUMN checked_at DATETIME(6);

INSERT INTO mci_schema_migrations (name, executed_at)
VALUES ('sakai_25_1_mci_02.sql', NOW(6));