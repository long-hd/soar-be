-- =====================================================================
-- V1_0_5: Seed dictionary data for Infrastructure module
-- =====================================================================

-- Dict type: API Error Log Process Status
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted)
VALUES (12, 'Error Log Process Status', 'infra_api_error_log_process_status', 0,
        'Processing status for API error logs', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- Dict type: Config Type
INSERT INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted)
VALUES (13, 'Config Type', 'infra_config_type', 0,
        'System configuration parameter type', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- Dict data: infra_api_error_log_process_status
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES
    (37, 1, 'Unprocessed', '0', 'infra_api_error_log_process_status', 0, 'warning', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (38, 2, 'Processed',   '1', 'infra_api_error_log_process_status', 0, 'success', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (39, 3, 'Ignored',     '2', 'infra_api_error_log_process_status', 0, 'info',    '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- Dict data: infra_config_type
INSERT INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES
    (40, 1, 'System Built-in', '1', 'infra_config_type', 0, 'danger',  '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false),
    (41, 2, 'Custom',          '2', 'infra_config_type', 0, 'primary', '', NULL, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);