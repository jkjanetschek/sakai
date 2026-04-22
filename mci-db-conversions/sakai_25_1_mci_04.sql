-- Make sure site page property for tool 'Overview' is set

Insert into sakai_site_page_property (site_id, page_id, name, value)  
Select page.SITE_ID, page.PAGE_ID, 'is_home_page', 'true' from sakai_site_page as page
where (lower(page.TITLE) Like 'overview' or lower(page.TITLE) like 'home')
and page.SITE_ID not like '!%'  
and not exists(
	SELECT 1 from sakai_site_page_property as prop2  
	where prop2.PAGE_ID = page.PAGE_ID  
	and prop2.NAME = 'is_home_page'
);

INSERT INTO mci_schema_migrations (name, executed_at)
VALUES ('sakai_25_1_mci_04.sql', NOW(6));