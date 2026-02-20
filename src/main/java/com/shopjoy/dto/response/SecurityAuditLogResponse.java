package com.shopjoy.dto.response;

import com.shopjoy.entity.SecurityEventType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for SecurityAuditLog response.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityAuditLogResponse {
    private Long id;
    private String username;
    private SecurityEventType eventType;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
    private String details;
    private Boolean success;
}
