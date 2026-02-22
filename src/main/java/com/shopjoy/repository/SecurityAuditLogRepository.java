package com.shopjoy.repository;

import com.shopjoy.entity.SecurityAuditLog;
import com.shopjoy.entity.SecurityEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for SecurityAuditLog entity.
 * Provides methods for querying security audit logs.
 */
@Repository
public interface SecurityAuditLogRepository extends JpaRepository<SecurityAuditLog, Long> {


    Page<SecurityAuditLog> findByUsername(String username, Pageable pageable);

    Page<SecurityAuditLog> findByEventType(SecurityEventType eventType, Pageable pageable);

    Page<SecurityAuditLog> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    Page<SecurityAuditLog> findByUsernameAndEventType(String username, SecurityEventType eventType, Pageable pageable);


    @Query("SELECT s FROM SecurityAuditLog s WHERE s.username = :username AND s.eventType = :eventType AND s.timestamp >= :since ORDER BY s.timestamp DESC")
    List<SecurityAuditLog> findRecentFailedAttempts(@Param("username") String username,
                                                     @Param("eventType") SecurityEventType eventType,
                                                     @Param("since") LocalDateTime since);


    @Query("SELECT COUNT(s) FROM SecurityAuditLog s WHERE s.eventType = :eventType AND s.timestamp BETWEEN :startTime AND :endTime")
    Long countByEventTypeAndTimestampBetween(@Param("eventType") SecurityEventType eventType,
                                             @Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);


}
