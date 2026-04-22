/*
    run before sakai_25_0_mysql.sql 
*/

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS
    scorm_activity_tree_holder_t,
    scorm_adl_valid_requests_t,
    scorm_attempt_t,
    scorm_content_package_t,
    scorm_cp_manifest_t,
    scorm_datamanager_t,
    scorm_datamodel_t,
    scorm_delimit_desc_t,
    scorm_delimiter_t,
    scorm_element_desc_t,
    scorm_element_t,
    scorm_launch_data_t,
    scorm_list_bindings_t,
    scorm_list_delim_desc_t,
    scorm_list_delimiters_t,
    scorm_list_elem_desc_t,
    scorm_list_elements_t,
    scorm_list_launch_data_t,
    scorm_list_records_t,
    scorm_map_children_t,
    scorm_map_datamodels_t,
    scorm_map_elements_t,
    scorm_map_sco_datamanager_t,
    scorm_type_validator_t,
    scorm_url;
SET FOREIGN_KEY_CHECKS = 1;


INSERT INTO mci_schema_migrations (name, executed_at)
VALUES ('sakai_23_4-25_0_mci_03.sql', NOW(6));