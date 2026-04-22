-- In order to distinguish the current external tool "JupyterHub" from the new variant we want better naming

update sakai.lti_tools set title = 'JupyterHub (Legacy)' where title = 'JupyterHub';
update sakai.lti_content set title = 'JupyterHub (Legacy)' where title = 'JupyterHub';
update sakai.sakai_site_tool set title = 'JupyterHub (Legacy)' where title = 'JupyterHub';
update sakai.sakai_site_page set title = 'JupyterHub (Legacy)' where title = 'JupyterHub';

INSERT INTO mci_schema_migrations (name, executed_at)
VALUES ('sakai_25_1_mci_03.sql', NOW(6));