package com.hdl.soar.framework.jpa.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;

/**
 * Base entity with audit fields and soft delete. PO stand for persistent object
 *
 * <p>All entities should extend this class to inherit:
 * <ul>
 *   <li>Audit fields: createdBy, createdAt, updatedBy, updatedAt (auto-filled by JPA Auditing)</li>
 *   <li>Soft delete: deleted flag</li>
 * </ul>
 *
 * <p>Subclasses define their own {@code @Id} field since ID strategy may vary
 * (IDENTITY for most entities, SEQUENCE for specific cases).
 */
@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("deleted = false")
public abstract class BasePO implements Serializable {

    /**
     * Creator user ID, auto-filled on insert by {@link AuditingEntityListener}.
     */
    @CreatedBy
    @Column(name = "creator", updatable = false)
    private Long creator;

    /**
     * Updater user ID, auto-filled on insert and update by {@link AuditingEntityListener}.
     */
    @LastModifiedBy
    @Column(name = "updater")
    private Long updater;

    /**
     * Creation timestamp in UTC, auto-filled on insert.
     * Database column: {@code timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP}
     */
    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    private Instant createTime;

    /**
     * Last update timestamp in UTC, auto-filled on insert and update.
     * Database column: {@code timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP}
     */
    @LastModifiedDate
    @Column(name = "update_time", nullable = false)
    private Instant updateTime;

    /**
     * Soft delete flag. {@code false} = active, {@code true} = deleted.
     * Database column: {@code boolean NOT NULL DEFAULT false}
     *
     * <p>Queries should filter by {@code deleted = false}.
     * Use {@code @SQLRestriction("deleted = false")} on entity class
     * or global Hibernate filter in starter-jpa config.
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = Boolean.FALSE;

}
