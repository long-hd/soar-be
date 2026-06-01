-- =====================================================================
-- V1_0_5: Seed dictionary data for Infrastructure module
-- =====================================================================

-- Dict types
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES
    ('Error Log Process Status', 'infra_api_error_log_process_status', 0,
     'Processing status for API error logs', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    ('Config Type', 'infra_config_type', 0,
     'System configuration parameter type', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- Dict data: infra_api_error_log_process_status
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES
    (1, 'Unprocessed', '0', 'infra_api_error_log_process_status', 0, 'warning', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2, 'Processed',   '1', 'infra_api_error_log_process_status', 0, 'success', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (3, 'Ignored',     '2', 'infra_api_error_log_process_status', 0, 'info',    '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- Dict data: infra_config_type
INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES
    (1, 'System Built-in', '1', 'infra_config_type', 0, 'danger',  '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (2, 'Custom',          '2', 'infra_config_type', 0, 'primary', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);
