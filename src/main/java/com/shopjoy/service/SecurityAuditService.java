package com.shopjoy.service;

import com.shopjoy.entity.SecurityAuditLog;
import com.shopjoy.entity.SecurityEventType;
import com.shopjoy.repository.SecurityAuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing security audit logs.
 * Handles logging of security events and querying audit trails.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityAuditService {

    private final SecurityAuditLogRepository auditLogRepository;

    /**
     * Log a security event asynchronously.
     *
     * @param username  the username (can be null for anonymous events)
     * @param eventType the type of security event
     * @param request   the HTTP request (used to extract IP and user agent)
     * @param details   additional details about the event
     * @param success   whether the event was successful
     */
    @Async
    @Transactional
    public void logEvent(String username, SecurityEventType eventType, HttpServletRequest request, String details, Boolean success) {
        try {
            SecurityAuditLog auditLog = SecurityAuditLog.builder()
                    .username(username)
                    .eventType(eventType)
                    .ipAddress(extractClientIp(request))
                    .userAgent(extractUserAgent(request))
                    .details(details)
                    .success(success)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Logged security event: {} for user: {}", eventType, username);
        } catch (Exception e) {
            log.error("Failed to log security event: {} for user: {}", eventType, username, e);
        }
    }

    /**
     * Log a security event without HTTP request context.
     *
     * @param username  the username
     * @param eventType the type of security event
     * @param details   additional details about the event
     * @param success   whether the event was successful
     */
    @Async
    @Transactional
    public void logEvent(String username, SecurityEventType eventType, String details, Boolean success) {
        try {
            SecurityAuditLog auditLog = SecurityAuditLog.builder()
                    .username(username)
                    .eventType(eventType)
                    .details(details)
                    .success(success)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Logged security event: {} for user: {}", eventType, username);
        } catch (Exception e) {
            log.error("Failed to log security event: {} for user: {}", eventType, username, e);
        }
    }

    /**
     * Extract client IP address from HTTP request.
     * Handles X-Forwarded-For header for proxied requests.
     *
     * @param request the HTTP request
     * @return the client IP address
     */
    private String extractClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        // Check for X-Forwarded-For header (for proxied requests)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take the first IP in the chain
            return xForwardedFor.split(",")[0].trim();
        }

        // Check for X-Real-IP header
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // Fall back to remote address
        return request.getRemoteAddr();
    }

    /**
     * Extract user agent from HTTP request.
     *
     * @param request the HTTP request
     * @return the user agent string
     */
    private String extractUserAgent(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String userAgent = request.getHeader("User-Agent");
        // Truncate if too long
        if (userAgent != null && userAgent.length() > 500) {
            return userAgent.substring(0, 500);
        }
        return userAgent;
    }

    /**
     * Get all audit logs with pagination.
     *
     * @param pageable pagination information
     * @return page of audit logs
     */
    @Transactional(readOnly = true)
    public Page<SecurityAuditLog> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    /**
     * Get audit logs by username.
     *
     * @param username the username
     * @param pageable pagination information
     * @return page of audit logs
     */
    @Transactional(readOnly = true)
    public Page<SecurityAuditLog> getLogsByUsername(String username, Pageable pageable) {
        return auditLogRepository.findByUsername(username, pageable);
    }

    /**
     * Get audit logs by event type.
     *
     * @param eventType the event type
     * @param pageable  pagination information
     * @return page of audit logs
     */
    @Transactional(readOnly = true)
    public Page<SecurityAuditLog> getLogsByEventType(SecurityEventType eventType, Pageable pageable) {
        return auditLogRepository.findByEventType(eventType, pageable);
    }

    /**
     * Get audit logs within a date range.
     *
     * @param startTime start timestamp
     * @param endTime   end timestamp
     * @param pageable  pagination information
     * @return page of audit logs
     */
    @Transactional(readOnly = true)
    public Page<SecurityAuditLog> getLogsByDateRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return auditLogRepository.findByTimestampBetween(startTime, endTime, pageable);
    }

    /**
     * Get audit logs by username and event type.
     *
     * @param username  the username
     * @param eventType the event type
     * @param pageable  pagination information
     * @return page of audit logs
     */
    @Transactional(readOnly = true)
    public Page<SecurityAuditLog> getLogsByUsernameAndEventType(String username, SecurityEventType eventType, Pageable pageable) {
        return auditLogRepository.findByUsernameAndEventType(username, eventType, pageable);
    }

    /**
     * Get recent failed login attempts for a username.
     *
     * @param username the username
     * @param minutes  number of minutes to look back
     * @return list of failed login attempts
     */
    @Transactional(readOnly = true)
    public List<SecurityAuditLog> getRecentFailedLogins(String username, int minutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutes);
        return auditLogRepository.findRecentFailedAttempts(username, SecurityEventType.LOGIN_FAILURE, since);
    }

    /**
     * Count security events by type within a time range.
     *
     * @param eventType the event type
     * @param startTime start timestamp
     * @param endTime   end timestamp
     * @return count of events
     */
    @Transactional(readOnly = true)
    public Long countEventsByType(SecurityEventType eventType, LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogRepository.countByEventTypeAndTimestampBetween(eventType, startTime, endTime);
    }
}
