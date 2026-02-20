package com.shopjoy.service;

/**
 * Service for rate limiting login attempts to prevent brute force attacks.
 * Tracks attempts by both username and IP address.
 */
public interface RateLimitService {
    
    /**
     * Records a failed login attempt for the given username and IP address.
     *
     * @param username the username that failed authentication
     * @param ipAddress the IP address of the client
     */
    void recordLoginAttempt(String username, String ipAddress);
    
    /**
     * Checks if the given username or IP address is rate limited.
     *
     * @param username the username to check
     * @param ipAddress the IP address to check
     * @return true if rate limited, false otherwise
     */
    boolean isRateLimited(String username, String ipAddress);
    
    /**
     * Resets the login attempt counter for successful authentication.
     *
     * @param username the username that successfully authenticated
     * @param ipAddress the IP address of the client
     */
    void resetAttempts(String username, String ipAddress);
    
    /**
     * Gets the number of seconds until the rate limit expires.
     *
     * @param username the username to check
     * @param ipAddress the IP address to check
     * @return seconds until unblock, or 0 if not rate limited
     */
    long getRetryAfterSeconds(String username, String ipAddress);
    
    /**
     * Gets the current size of the rate limit tracking map.
     *
     * @return the number of entries in the tracking map
     */
    int getTrackingSize();
    
    /**
     * Removes all expired entries from the tracking map.
     * This method is called automatically on a schedule.
     */
    void removeExpiredEntries();
}
