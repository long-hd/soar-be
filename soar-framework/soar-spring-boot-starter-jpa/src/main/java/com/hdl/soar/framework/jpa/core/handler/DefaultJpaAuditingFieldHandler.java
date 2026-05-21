package com.hdl.soar.framework.jpa.core.handler;

import com.hdl.soar.framework.jpa.core.entity.BasePO;
import com.hdl.soar.framework.security.core.util.SecurityFrameworkUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;


/**
 * JPA Auditing configuration.
 *
 * <p>Enables auto-population of {@code @CreatedBy}, {@code @LastModifiedBy},
 * {@code @CreatedDate}, {@code @LastModifiedDate} on entities extending {@link
 * BasePO}
 *
 * <p>{@code @CreatedDate} and {@code @LastModifiedDate} are handled automatically
 * by Spring Data JPA (reads from system clock).
 *
 * <p>{@code @CreatedBy} and {@code @LastModifiedBy} require this {@link AuditorAware}
 * bean to resolve the current user ID from Spring Security context.
 */
@AutoConfiguration
@EnableJpaAuditing
public class DefaultJpaAuditingFieldHandler implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        return Optional.ofNullable(SecurityFrameworkUtils.getLoginUserId());
    }

}
