package com.hdl.soar.module.infra.dal.entity.file;

import com.hdl.soar.framework.jpa.core.entity.BasePO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * File content for DB-backed storage. One row per uploaded file (latest id wins on read).
 * <p>
 * Extends {@link BasePO} (not TenantBasePO) — global.
 */
@Entity
@Table(name = "infra_file_content")
@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class FileContentPO extends BasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * File config id (references {@code infra_file_config.id}).
     */
    @Column(name = "config_id", nullable = false)
    private Long configId;

    /**
     * Relative storage path (key).
     */
    @Column(name = "path", nullable = false)
    private String path;

    /**
     * File bytes (PostgreSQL {@code bytea}).
     */
    @Column(name = "content", nullable = false)
    private byte[] content;

}
