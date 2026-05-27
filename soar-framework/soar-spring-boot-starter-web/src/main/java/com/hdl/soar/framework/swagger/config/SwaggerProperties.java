package com.hdl.soar.framework.swagger.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Swagger configuration properties
 */
@Data
@ConfigurationProperties("soar.swagger")
public class SwaggerProperties {

    /**
     * Title
     */
    @NotEmpty(message = "Title cannot be empty")
    private String title;

    /**
     * Description
     */
    @NotEmpty(message = "Description cannot be empty")
    private String description;

    /**
     * Author
     */
    @NotEmpty(message = "Author cannot be empty")
    private String author;

    /**
     * Version
     */
    @NotEmpty(message = "Version cannot be empty")
    private String version;

    /**
     * URL
     */
    @NotEmpty(message = "Scanned package URL cannot be empty")
    private String url;

    /**
     * Email
     */
    @NotEmpty(message = "Scanned email cannot be empty")
    private String email;

    /**
     * License
     */
    @NotEmpty(message = "Scanned license cannot be empty")
    private String license;

    /**
     * License URL
     */
    @NotEmpty(message = "Scanned license URL cannot be empty")
    private String licenseUrl;

}
