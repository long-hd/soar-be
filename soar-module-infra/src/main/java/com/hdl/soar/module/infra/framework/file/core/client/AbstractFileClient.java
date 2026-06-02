package com.hdl.soar.module.infra.framework.file.core.client;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base for file clients. Provides the init/refresh template and URL formatting,
 * reducing boilerplate in concrete clients.
 *
 * @param <Config> the storage-specific config type
 */
@Slf4j
public abstract class AbstractFileClient<Config extends FileClientConfig> implements FileClient {

    /**
     * Config id.
     */
    private final Long id;

    /**
     * Current config (may be mutated by subclasses during {@link #doInit()}).
     */
    protected Config config;

    /**
     * Original config snapshot, used to detect changes in {@link #refresh(FileClientConfig)}.
     * Kept separate because {@link #config} can be mutated by subclasses.
     */
    private Config originalConfig;

    public AbstractFileClient(Long id, Config config) {
        this.id = id;
        this.config = config;
        this.originalConfig = config;
    }

    /**
     * Initialize the client (idempotent entry point).
     */
    public final void init() {
        doInit();
        log.debug("[init][config({}) initialized]", config);
    }

    /**
     * Subclass-specific initialization.
     */
    protected abstract void doInit();

    /**
     * Refresh with a (possibly) new config. Re-initializes only if the config actually changed.
     */
    public final void refresh(Config config) {
        if (config.equals(this.originalConfig)) {
            return;
        }
        log.info("[refresh][config({}) changed, re-initializing]", config);
        this.config = config;
        this.originalConfig = config;
        this.init();
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * Build the HTTP URL for accessing a file via the infra file controller.
     * Used by local/db backends (S3 returns its own presigned/public URL).
     *
     * @param domain custom domain (base URL)
     * @param path   relative path
     * @return URL like {@code {domain}/admin-api/infra/file/{id}/get/{path}}
     */
    protected String formatFileUrl(String domain, String path) {
        return StrUtil.format("{}/admin-api/infra/file/{}/get/{}", domain, getId(), path);
    }

}
