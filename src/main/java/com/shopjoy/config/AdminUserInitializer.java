package com.shopjoy.config;

import com.shopjoy.dto.request.CreateUserRequest;
import com.shopjoy.dto.response.UserResponse;
import com.shopjoy.entity.UserType;
import com.shopjoy.exception.DuplicateResourceException;
import com.shopjoy.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * CommandLineRunner implementation that creates a default admin user on application startup.
 * 
 * This component:
 * - Runs after the application context is fully initialized
 * - Checks if an admin user already exists using the configured username
 * - Creates a new admin user only if one doesn't exist
 * - Uses configurable properties for admin credentials
 * - Implements proper error handling and logging
 * - Prevents duplicate admin users on application restarts
 * 
 * Configuration:
 * - Enable/disable with: app.admin.enabled=true/false
 * - Override credentials with app.admin.* properties
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class AdminUserInitializer implements CommandLineRunner {

    private final UserService userService;
    private final AdminUserProperties adminProperties;

    @Override
    public void run(String @NonNull ... args) {
        if (!adminProperties.isEnabled()) {
            log.info("Admin user initialization is disabled (app.admin.enabled=false)");
            return;
        }

        try {
            createDefaultAdminUser();
        } catch (Exception e) {
            log.error("Failed to initialize admin user during startup", e);
        }
    }


    private void createDefaultAdminUser() {
        String adminUsername = adminProperties.getUsername();
        
        log.info("Checking for existing admin user with username: {}", adminUsername);
        
        if (userService.isUsernameTaken(adminUsername)) {
            log.info("Admin user '{}' already exists, skipping creation", adminUsername);
            return;
        }

        if (hasExistingAdminUser()) {
            log.info("At least one admin user already exists in the system, skipping default admin creation");
            return;
        }

        log.info("No admin user found, creating default admin user...");
        
        try {
            UserResponse createdUser = createAdminUser();
            log.info("Successfully created admin user: {} (ID: {}, Email: {})",
                    createdUser.getUsername(),
                    createdUser.getId(),
                    createdUser.getEmail());
            
        } catch (DuplicateResourceException e) {
            log.warn("Admin user creation skipped - user already exists: {}", e.getMessage());
            
        } catch (Exception e) {
            log.error("Failed to create admin user: {}", e.getMessage(), e);
            throw new RuntimeException("Admin user creation failed", e);
        }
    }

    /**
     * Creates the admin user with configured credentials.
     */
    private UserResponse createAdminUser() {
        CreateUserRequest adminRequest = CreateUserRequest.builder()
                .username(adminProperties.getUsername())
                .email(adminProperties.getEmail())
                .password(adminProperties.getPassword())
                .firstName(adminProperties.getFirstName())
                .lastName(adminProperties.getLastName())
                .phone(adminProperties.getPhone())
                .build();

        log.debug("Creating admin user with username: {}, email: {}", 
                adminRequest.getUsername(), adminRequest.getEmail());

        return userService.registerUser(adminRequest, UserType.ADMIN);
    }

    /**
     * Checks if there are any existing admin users in the system.
     * This provides an additional safety check to prevent creating multiple admin accounts.
     */
    private boolean hasExistingAdminUser() {
        try {
            var adminUsers = userService.getUsersByType(UserType.ADMIN);
            boolean hasAdmins = !adminUsers.isEmpty();
            
            if (hasAdmins) {
                log.debug("Found {} existing admin user(s)", adminUsers.size());
            }
            
            return hasAdmins;
            
        } catch (Exception e) {
            log.warn("Could not check for existing admin users: {}", e.getMessage());
            return false;
        }
    }
}