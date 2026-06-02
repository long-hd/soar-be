package com.hdl.soar.module.infra.framework.file.core.client;

/**
 * Marker interface for storage-specific client configuration.
 *
 * <p>Unlike a polymorphic-JSON approach, the concrete config type is NOT encoded in the
 * stored JSON. Instead it is resolved from {@code FileConfigPO.storage} via
 * {@link com.hdl.soar.module.infra.framework.file.core.enums.FileStorageEnum}. As a result the
 * JSON persisted in {@code infra_file_config.config} carries no {@code @class} field, and the
 * database is not coupled to Java package/class names.
 *
 * <p>Consequence: every site that deserializes config must supply the {@code storage} value
 * alongside the JSON (always available, since both live on {@code FileConfigPO}).
 */
public interface FileClientConfig {
}