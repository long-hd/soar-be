package com.hdl.soar.module.infra.enums;

import com.hdl.soar.framework.common.exception.ErrorCode;

/**
 * Infra module error codes.
 * <p>
 * Code range: [1-001-000-000, 1-002-000-000)
 */
public interface ErrorCodeConstants {

    // ========== Parameter Configuration 1-001-000-000 ==========
    ErrorCode CONFIG_NOT_EXISTS = new ErrorCode(1_001_000_001, "Parameter configuration does not exist");
    ErrorCode CONFIG_KEY_DUPLICATE = new ErrorCode(1_001_000_002, "Duplicate parameter configuration key");
    ErrorCode CONFIG_CAN_NOT_DELETE_SYSTEM_TYPE = new ErrorCode(1_001_000_003, "Cannot delete a system built-in parameter configuration");
    ErrorCode CONFIG_GET_VALUE_ERROR_IF_VISIBLE = new ErrorCode(1_001_000_004, "Failed to get parameter configuration: access to invisible configuration is not allowed");

    // ========== Scheduled Jobs 1-001-001-000 ==========
    ErrorCode JOB_NOT_EXISTS = new ErrorCode(1_001_001_000, "Scheduled job does not exist");
    ErrorCode JOB_HANDLER_EXISTS = new ErrorCode(1_001_001_001, "The job handler already exists");
    ErrorCode JOB_CHANGE_STATUS_INVALID = new ErrorCode(1_001_001_002, "Status can only be changed to enabled or disabled");
    ErrorCode JOB_CHANGE_STATUS_EQUALS = new ErrorCode(1_001_001_003, "The scheduled job is already in the target status");
    ErrorCode JOB_UPDATE_ONLY_NORMAL_STATUS = new ErrorCode(1_001_001_004, "Only enabled jobs can be modified");
    ErrorCode JOB_CRON_EXPRESSION_VALID = new ErrorCode(1_001_001_005, "Invalid CRON expression");
    ErrorCode JOB_HANDLER_BEAN_NOT_EXISTS = new ErrorCode(1_001_001_006, "Job handler Bean does not exist. Note: Bean names are lowercase by default");
    ErrorCode JOB_HANDLER_BEAN_TYPE_ERROR = new ErrorCode(1_001_001_007, "Invalid job handler Bean type. It does not implement the JobHandler interface");
    ErrorCode JOB_LOG_NOT_EXISTS = new ErrorCode(1_001_001_008, "Job log does not exist");

    // ========== API Error Logs 1-001-002-000 ==========
    ErrorCode API_ERROR_LOG_NOT_FOUND = new ErrorCode(1_001_002_000, "API error log does not exist");
    ErrorCode API_ERROR_LOG_PROCESSED = new ErrorCode(1_001_002_001, "API error log has already been processed");

    // ========== File Related 1-001-003-000 ==========
    ErrorCode FILE_PATH_EXISTS = new ErrorCode(1_001_003_000, "File path already exists");
    ErrorCode FILE_NOT_EXISTS = new ErrorCode(1_001_003_001, "File does not exist");
    ErrorCode FILE_IS_EMPTY = new ErrorCode(1_001_003_002, "File is empty");
    ErrorCode FILE_PRESIGN_NOT_SUPPORTED = new ErrorCode(1_001_003_003, "Presigned upload is not supported by the current storage (only S3)");

    // ========== Code Generator 1-001-004-000 ==========
    ErrorCode CODEGEN_TABLE_EXISTS = new ErrorCode(1_001_004_002, "Table definition already exists");
    ErrorCode CODEGEN_IMPORT_TABLE_NULL = new ErrorCode(1_001_004_001, "Imported table does not exist");
    ErrorCode CODEGEN_IMPORT_COLUMNS_NULL = new ErrorCode(1_001_004_002, "Imported columns do not exist");
    ErrorCode CODEGEN_TABLE_NOT_EXISTS = new ErrorCode(1_001_004_004, "Table definition does not exist");
    ErrorCode CODEGEN_COLUMN_NOT_EXISTS = new ErrorCode(1_001_004_005, "Column definition does not exist");
    ErrorCode CODEGEN_SYNC_COLUMNS_NULL = new ErrorCode(1_001_004_006, "Columns to synchronize do not exist");
    ErrorCode CODEGEN_SYNC_NONE_CHANGE = new ErrorCode(1_001_004_007, "Synchronization failed: no changes detected");
    ErrorCode CODEGEN_TABLE_INFO_TABLE_COMMENT_IS_NULL = new ErrorCode(1_001_004_008, "Database table comment is missing");
    ErrorCode CODEGEN_TABLE_INFO_COLUMN_COMMENT_IS_NULL = new ErrorCode(1_001_004_009, "Database column ({}) comment is missing");
    ErrorCode CODEGEN_MASTER_TABLE_NOT_EXISTS = new ErrorCode(1_001_004_010, "Master table definition (id={}) does not exist, please check");
    ErrorCode CODEGEN_SUB_COLUMN_NOT_EXISTS = new ErrorCode(1_001_004_011, "Sub-table column (id={}) does not exist, please check");
    ErrorCode CODEGEN_MASTER_GENERATION_FAIL_NO_SUB_TABLE = new ErrorCode(1_001_004_012, "Failed to generate code for the master table: no sub-table exists");

    // ========== File Configuration 1-001-006-000 ==========
    ErrorCode FILE_CONFIG_NOT_EXISTS = new ErrorCode(1_001_006_000, "File configuration does not exist");
    ErrorCode FILE_CONFIG_DELETE_FAIL_MASTER = new ErrorCode(1_001_006_001, "This file configuration cannot be deleted because it is the primary configuration. Deleting it would prevent file uploads");

    // ========== Data Source Configuration 1-001-007-000 ==========
    ErrorCode DATA_SOURCE_CONFIG_NOT_EXISTS = new ErrorCode(1_001_007_000, "Data source configuration does not exist");
    ErrorCode DATA_SOURCE_CONFIG_NOT_OK = new ErrorCode(1_001_007_001, "Invalid data source configuration. Connection cannot be established");

    // ========== Student Demo 1-001-201-000 ==========
    ErrorCode DEMO01_CONTACT_NOT_EXISTS = new ErrorCode(1_001_201_000, "Demo contact does not exist");
    ErrorCode DEMO02_CATEGORY_NOT_EXISTS = new ErrorCode(1_001_201_001, "Demo category does not exist");
    ErrorCode DEMO02_CATEGORY_EXITS_CHILDREN = new ErrorCode(1_001_201_002, "Cannot delete because child demo categories exist");
    ErrorCode DEMO02_CATEGORY_PARENT_NOT_EXITS = new ErrorCode(1_001_201_003, "Parent demo category does not exist");
    ErrorCode DEMO02_CATEGORY_PARENT_ERROR = new ErrorCode(1_001_201_004, "Cannot set itself as its own parent category");
    ErrorCode DEMO02_CATEGORY_NAME_DUPLICATE = new ErrorCode(1_001_201_005, "A demo category with this name already exists");
    ErrorCode DEMO02_CATEGORY_PARENT_IS_CHILD = new ErrorCode(1_001_201_006, "Cannot set one of its child categories as the parent category");
    ErrorCode DEMO03_STUDENT_NOT_EXISTS = new ErrorCode(1_001_201_007, "Student does not exist");
    ErrorCode DEMO03_COURSE_NOT_EXISTS = new ErrorCode(1_001_201_008, "Student course does not exist");
    ErrorCode DEMO03_GRADE_NOT_EXISTS = new ErrorCode(1_001_201_009, "Student class does not exist");
    ErrorCode DEMO03_GRADE_EXISTS = new ErrorCode(1_001_201_010, "Student class already exists");

}
