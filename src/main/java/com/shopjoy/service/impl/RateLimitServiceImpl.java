package com.shopjoy.service.impl;

import com.shopjoy.service.RateLimitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementation of RateLimitService that tracks login attempts using ConcurrentHashMap.
 * Prevents brute force attacks by limiting login attempts to 5 per 15 minutes.
 * Tracks both username and IP address for comprehensive security.
 */
@Slf4j
@Service
public class RateLimitServiceImpl implements RateLimitService {
    
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;
    private static final int MAX_TRACKING_SIZE = 50000;
    
    private final ConcurrentMap<String, LoginAttempt> attemptsByUsername = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LoginAttempt> attemptsByIp = new ConcurrentHashMap<>();
    
    private static class LoginAttempt {
        int count;
        LocalDateTime firstAttemptTime;
        LocalDateTime lastAttemptTime;
        
        LoginAttempt() {
            this.count = 1;
            this.firstAttemptTime = LocalDateTime.now();
            this.lastAttemptTime = LocalDateTime.now();
        }
        
        void increment() {
            this.count++;
            this.lastAttemptTime = LocalDateTime.now();
        }
        
        boolean isExpired() {
            return lastAttemptTime.plusMinutes(LOCKOUT_DURATION_MINUTES).isBefore(LocalDateTime.now());
        }
        
        boolean isRateLimited() {
            if (isExpired()) {
                return false;
            }
            return count >= MAX_ATTEMPTS;
        }
        
        long getRetryAfterSeconds() {
            if (!isRateLimited()) {
                return 0;
            }
            LocalDateTime unlockTime = lastAttemptTime.plusMinutes(LOCKOUT_DURATION_MINUTES);
            long secondsUntilUnlock = java.time.Duration.between(LocalDateTime.now(), unlockTime).getSeconds();
            return Math.max(0, secondsUntilUnlock);
        }
    }
    
    @Override
    public void recordLoginAttempt(String username, String ipAddress) {
        if (username != null && !username.isBlank()) {
            attemptsByUsername.compute(username, (key, attempt) -> {
                if (attempt == null || attempt.isExpired()) {
                    return new LoginAttempt();
                }
                attempt.increment();
                return attempt;
            });
            
            log.debug("Recorded login attempt for username: {}. Count: {}", 
                username, attemptsByUsername.get(username).count);
        }
        
        if (ipAddress != null && !ipAddress.isBlank()) {
            attemptsByIp.compute(ipAddress, (key, attempt) -> {
                if (attempt == null || attempt.isExpired()) {
                    return new LoginAttempt();
                }
                attempt.increment();
                return attempt;
            });
            
            log.debug("Recorded login attempt for IP: {}. Count: {}", 
                ipAddress, attemptsByIp.get(ipAddress).count);
        }
        
        checkMapSizes();
    }
    
    @Override
    public boolean isRateLimited(String username, String ipAddress) {
        boolean usernameBlocked = false;
        boolean ipBlocked = false;
        
        if (username != null && !username.isBlank()) {
            LoginAttempt attempt = attemptsByUsername.get(username);
            if (attempt != null && attempt.isRateLimited()) {
                usernameBlocked = true;
                log.warn("Username '{}' is rate limited. Attempts: {}", username, attempt.count);
            }
        }
        
        if (ipAddress != null && !ipAddress.isBlank()) {
            LoginAttempt attempt = attemptsByIp.get(ipAddress);
            if (attempt != null && attempt.isRateLimited()) {
                ipBlocked = true;
                log.warn("IP address '{}' is rate limited. Attempts: {}", ipAddress, attempt.count);
            }
        }
        
        return usernameBlocked || ipBlocked;
    }
    
    @Override
    public void resetAttempts(String username, String ipAddress) {
        if (username != null && !username.isBlank()) {
            LoginAttempt removed = attemptsByUsername.remove(username);
            if (removed != null) {
                log.debug("Reset login attempts for username: {}. Previous count: {}", username, removed.count);
            }
        }
        
        if (ipAddress != null && !ipAddress.isBlank()) {
            LoginAttempt removed = attemptsByIp.remove(ipAddress);
            if (removed != null) {
                log.debug("Reset login attempts for IP: {}. Previous count: {}", ipAddress, removed.count);
            }
        }
    }
    
    @Override
    public long getRetryAfterSeconds(String username, String ipAddress) {
        long maxRetryAfter = 0;
        
        if (username != null && !username.isBlank()) {
            LoginAttempt attempt = attemptsByUsername.get(username);
            if (attempt != null) {
                maxRetryAfter = Math.max(maxRetryAfter, attempt.getRetryAfterSeconds());
            }
        }
        
        if (ipAddress != null && !ipAddress.isBlank()) {
            LoginAttempt attempt = attemptsByIp.get(ipAddress);
            if (attempt != null) {
                maxRetryAfter = Math.max(maxRetryAfter, attempt.getRetryAfterSeconds());
            }
        }
        
        return maxRetryAfter;
    }
    
    @Override
    public int getTrackingSize() {
        return attemptsByUsername.size() + attemptsByIp.size();
    }
    
    @Override
    @Scheduled(fixedRate = 1800000)
    public void removeExpiredEntries() {
        int removedUsername = 0;
        int removedIp = 0;
        
        var usernameIterator = attemptsByUsername.entrySet().iterator();
        while (usernameIterator.hasNext()) {
            var entry = usernameIterator.next();
            if (entry.getValue().isExpired()) {
                usernameIterator.remove();
                removedUsername++;
            }
        }
        
        var ipIterator = attemptsByIp.entrySet().iterator();
        while (ipIterator.hasNext()) {
            var entry = ipIterator.next();
            if (entry.getValue().isExpired()) {
                ipIterator.remove();
                removedIp++;
            }
        }
        
        if (removedUsername > 0 || removedIp > 0) {
            log.info("Rate limit cleanup: Removed {} username entries and {} IP entries. " +
                    "Remaining: {} username, {} IP", 
                    removedUsername, removedIp, attemptsByUsername.size(), attemptsByIp.size());
        }
    }
    
    private void checkMapSizes() {
        int totalSize = attemptsByUsername.size() + attemptsByIp.size();
        if (totalSize > MAX_TRACKING_SIZE * 0.9) {
            log.warn("Rate limit tracking maps approaching capacity: {}/{}", totalSize, MAX_TRACKING_SIZE);
        }
        
        if (totalSize > MAX_TRACKING_SIZE) {
            log.error("Rate limit tracking maps exceeded maximum size. Forcing cleanup.");
            removeExpiredEntries();
        }
    }
}
