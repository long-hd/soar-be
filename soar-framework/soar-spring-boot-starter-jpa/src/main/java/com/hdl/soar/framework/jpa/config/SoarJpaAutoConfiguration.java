package com.hdl.soar.framework.jpa.config;

import com.hdl.soar.framework.jpa.core.repository.SoftDeleteRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@EntityScan(basePackages = "com.hdl.soar")
@EnableJpaRepositories(
        basePackages = "com.hdl.soar",
        repositoryBaseClass = SoftDeleteRepository.class
)
public class SoarJpaAutoConfiguration {
}
