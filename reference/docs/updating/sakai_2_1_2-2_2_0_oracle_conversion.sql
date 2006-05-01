-- This is the Oracle Sakai 2.1.2 -> 2.2.0 conversion script
----------------------------------------------------------------------------------------------------------------------------------------
--
-- use this to convert a Sakai database from 2.1.2 to 2.2.0.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
----------------------------------------------------------------------------------------------------------------------------------------


-- ContentHosting
alter table CONTENT_COLLECTION add column COLLECTION_UUID varchar2 (36), add column ACCESS_MODE varchar2 (12), add column RELEASE_DATE DATETIME, add column RETRACT_DATE DATETIME, add column SORT_ORDER INT;
create index CONTENT_UUID_COLLECTION_INDEX on CONTENT_COLLECTION ( COLLECTION_UUID );
alter table CONTENT_RESOURCE add column ACCESS_MODE varchar2(12), add column RELEASE_DATE DATETIME, add column RETRACT_DATE DATETIME, add column SORT_ORDER INT;
create table CONTENT_ENTITY_GROUPS ( ENTITY_ID VARCHAR2 (36) NOT NULL, GROUP_ID VARCHAR2 (36) NOT NULL );
create index CONTENT_ENTITY_INDEX on CONTENT_ENTITY_GROUPS ( ENTITY_ID );
create index CONTENT_GROUP_INDEX on CONTENT_ENTITY_GROUPS ( GROUP_ID );
