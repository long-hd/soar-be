package com.hdl.soar.module.infra.dal.entity.file;

import com.hdl.soar.framework.jpa.core.entity.BasePO;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * File storage configuration.
 * <p>
 * Extends {@link BasePO} (not TenantBasePO) — config is global, not per-tenant.
 */
@Entity
@Table(name = "infra_file_config")
@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class FileConfigPO extends BasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Config name.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Storage type. Maps to {@code FileStorageEnum.storage}:
     * <ul>
     *   <li>1  = DB (bytes stored in {@code infra_file_content})</li>
     *   <li>10 = LOCAL (local disk)</li>
     *   <li>20 = S3 (S3-compatible: SeaweedFS, AWS S3)</li>
     * </ul>
     * Kept as {@code Integer} (not an enum) because {@code FileStorageEnum} is a framework-internal
     * concern (maps storage to client class) that the DAL layer should not import. The enum lookup
     * is done in the service via {@code FileStorageEnum.getByStorage(...)}.
     */
    @Column(name = "storage", nullable = false)
    private Integer storage;

    /**
     * Remark.
     */
    @Column(name = "remark")
    private String remark;

    /**
     * Whether this is the master (default) config. Exactly one config should be master.
     */
    @Column(name = "master", nullable = false)
    @Builder.Default
    private Boolean master = Boolean.FALSE;

    /**
     * Storage-specific config as JSON. The concrete shape is resolved from {@link #storage}
     * (see {@code FileClientConfig}); the JSON carries no {@code @class} field.
     */
    @Column(name = "config", nullable = false)
    private String config;

}
