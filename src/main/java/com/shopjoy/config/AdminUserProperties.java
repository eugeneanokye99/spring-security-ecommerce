package com.shopjoy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for default admin user creation.
 * These properties can be overridden in application.properties or environment variables.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.admin")
public class AdminUserProperties {

    /**
     * Default admin username.
     * Can be overridden with app.admin.username property.
     */
    private String username = "admin";

    /**
     * Default admin password (plain text, will be hashed).
     * Can be overridden with app.admin.password property.
     */
    private String password = "password123";

    /**
     * Default admin email address.
     * Can be overridden with app.admin.email property.
     */
    private String email = "admin@shopjoy.com";

    /**
     * Default admin first name.
     * Can be overridden with app.admin.firstName property.
     */
    private String firstName = "Admin";

    /**
     * Default admin last name.
     * Can be overridden with app.admin.lastName property.
     */
    private String lastName = "User";

    /**
     * Default admin phone number.
     * Can be overridden with app.admin.phone property.
     */
    private String phone = "555-0000";

    /**
     * Whether to create the default admin user on application startup.
     * Can be disabled by setting app.admin.enabled=false.
     */
    private boolean enabled = true;
}