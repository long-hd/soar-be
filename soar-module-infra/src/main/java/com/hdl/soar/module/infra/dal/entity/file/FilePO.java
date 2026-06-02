package com.hdl.soar.module.infra.dal.entity.file;

import com.hdl.soar.framework.jpa.core.entity.BasePO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Uploaded file metadata.
 * <p>
 * Extends {@link BasePO} (not TenantBasePO) — files are global, not per-tenant.
 */
@Entity
@Table(name = "infra_file")
@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class FilePO extends BasePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * File config id this file was uploaded through (references {@code infra_file_config.id}).
     */
    @Column(name = "config_id")
    private Long configId;

    /**
     * Original file name.
     */
    @Column(name = "name")
    private String name;

    /**
     * Relative storage path (key).
     */
    @Column(name = "path", nullable = false)
    private String path;

    /**
     * Full HTTP access URL.
     */
    @Column(name = "url", nullable = false)
    private String url;

    /**
     * Content type (MIME). for example, "application/octet-stream"
     */
    @Column(name = "type")
    private String type;

    /**
     * File size in bytes.
     */
    @Column(name = "size", nullable = false)
    private Integer size;

}
