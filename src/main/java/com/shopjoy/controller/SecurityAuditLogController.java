package com.shopjoy.controller;

import com.shopjoy.dto.response.ApiResponse;
import com.shopjoy.dto.response.SecurityAuditLogResponse;
import com.shopjoy.entity.SecurityAuditLog;
import com.shopjoy.entity.SecurityEventType;
import com.shopjoy.service.SecurityAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controller for managing and viewing security audit logs.
 * Only accessible to administrators.
 */
@Tag(name = "Security Audit Logs", description = "APIs for viewing and managing security audit logs (Admin only)")
@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SecurityAuditLogController {

    private final SecurityAuditService securityAuditService;

    /**
     * Get all audit logs with pagination.
     *
     * @param page the page number (0-indexed)
     * @param size the page size
     * @param sortBy the field to sort by
     * @param sortDir the sort direction (asc or desc)
     * @return paginated list of audit logs
     */
    @Operation(summary = "Get all audit logs", description = "Retrieve all security audit logs with pagination and sorting")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SecurityAuditLogResponse>>> getAllLogs(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "timestamp") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<SecurityAuditLog> logs = securityAuditService.getAllLogs(pageable);
        Page<SecurityAuditLogResponse> responsePage = logs.map(this::mapToResponse);
        
        return ResponseEntity.ok(ApiResponse.success(responsePage, "Audit logs retrieved successfully"));
    }

    /**
     * Get audit logs by username.
     *
     * @param username the username to filter by
     * @param page the page number
     * @param size the page size
     * @return paginated list of audit logs for the user
     */
    @Operation(summary = "Get audit logs by username", description = "Retrieve audit logs for a specific user")
    @GetMapping("/user/{username}")
    public ResponseEntity<ApiResponse<Page<SecurityAuditLogResponse>>> getLogsByUsername(
            @Parameter(description = "Username to filter by") @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<SecurityAuditLog> logs = securityAuditService.getLogsByUsername(username, pageable);
        Page<SecurityAuditLogResponse> responsePage = logs.map(this::mapToResponse);
        
        return ResponseEntity.ok(ApiResponse.success(responsePage, "User audit logs retrieved successfully"));
    }

    /**
     * Get audit logs by event type.
     *
     * @param eventType the event type to filter by
     * @param page the page number
     * @param size the page size
     * @return paginated list of audit logs for the event type
     */
    @Operation(summary = "Get audit logs by event type", description = "Retrieve audit logs for a specific event type")
    @GetMapping("/event-type/{eventType}")
    public ResponseEntity<ApiResponse<Page<SecurityAuditLogResponse>>> getLogsByEventType(
            @Parameter(description = "Event type to filter by") @PathVariable SecurityEventType eventType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<SecurityAuditLog> logs = securityAuditService.getLogsByEventType(eventType, pageable);
        Page<SecurityAuditLogResponse> responsePage = logs.map(this::mapToResponse);
        
        return ResponseEntity.ok(ApiResponse.success(responsePage, "Event type audit logs retrieved successfully"));
    }

    /**
     * Get audit logs within a date range.
     *
     * @param startTime start timestamp
     * @param endTime end timestamp
     * @param page the page number
     * @param size the page size
     * @return paginated list of audit logs within the date range
     */
    @Operation(summary = "Get audit logs by date range", description = "Retrieve audit logs within a specific date range")
    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<Page<SecurityAuditLogResponse>>> getLogsByDateRange(
            @Parameter(description = "Start date-time (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "End date-time (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<SecurityAuditLog> logs = securityAuditService.getLogsByDateRange(startTime, endTime, pageable);
        Page<SecurityAuditLogResponse> responsePage = logs.map(this::mapToResponse);
        
        return ResponseEntity.ok(ApiResponse.success(responsePage, "Date range audit logs retrieved successfully"));
    }

    /**
     * Get audit logs by username and event type.
     *
     * @param username the username to filter by
     * @param eventType the event type to filter by
     * @param page the page number
     * @param size the page size
     * @return paginated list of audit logs
     */
    @Operation(summary = "Get audit logs by username and event type", 
               description = "Retrieve audit logs for a specific user and event type")
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<Page<SecurityAuditLogResponse>>> getLogsByUsernameAndEventType(
            @Parameter(description = "Username to filter by") @RequestParam String username,
            @Parameter(description = "Event type to filter by") @RequestParam SecurityEventType eventType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<SecurityAuditLog> logs = securityAuditService.getLogsByUsernameAndEventType(username, eventType, pageable);
        Page<SecurityAuditLogResponse> responsePage = logs.map(this::mapToResponse);
        
        return ResponseEntity.ok(ApiResponse.success(responsePage, "Filtered audit logs retrieved successfully"));
    }

    /**
     * Get recent failed login attempts for a user.
     *
     * @param username the username
     * @param minutes number of minutes to look back
     * @return list of failed login attempts
     */
    @Operation(summary = "Get recent failed login attempts", 
               description = "Retrieve recent failed login attempts for a specific user")
    @GetMapping("/failed-logins/{username}")
    public ResponseEntity<ApiResponse<Object>> getRecentFailedLogins(
            @Parameter(description = "Username to check") @PathVariable String username,
            @Parameter(description = "Number of minutes to look back") @RequestParam(defaultValue = "60") int minutes
    ) {
        var failedLogins = securityAuditService.getRecentFailedLogins(username, minutes);
        var responseList = failedLogins.stream()
                .map(this::mapToResponse)
                .toList();
        
        return ResponseEntity.ok(ApiResponse.success(responseList, 
                "Recent failed login attempts retrieved successfully"));
    }

    /**
     * Count events by type within a time range.
     *
     * @param eventType the event type
     * @param startTime start timestamp
     * @param endTime end timestamp
     * @return count of events
     */
    @Operation(summary = "Count events by type", 
               description = "Count security events of a specific type within a time range")
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countEventsByType(
            @Parameter(description = "Event type to count") @RequestParam SecurityEventType eventType,
            @Parameter(description = "Start date-time (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "End date-time (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    ) {
        Long count = securityAuditService.countEventsByType(eventType, startTime, endTime);
        return ResponseEntity.ok(ApiResponse.success(count, "Event count retrieved successfully"));
    }

    /**
     * Map SecurityAuditLog entity to response DTO.
     */
    private SecurityAuditLogResponse mapToResponse(SecurityAuditLog log) {
        return SecurityAuditLogResponse.builder()
                .id(log.getId())
                .username(log.getUsername())
                .eventType(log.getEventType())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .timestamp(log.getTimestamp())
                .details(log.getDetails())
                .success(log.getSuccess())
                .build();
    }
}
