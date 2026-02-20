package com.shopjoy.service;

/**
 * Service interface for managing blacklisted JWT tokens (logged out tokens).
 * Provides methods for token revocation and blacklist management.
 */
public interface TokenBlacklistService {

    /**
     * Adds a token to the blacklist.
     * The token will remain blacklisted until its expiration time.
     * 
     * @param token the JWT token to blacklist
     */
    void blacklistToken(String token);

    /**
     * Checks if a token is blacklisted.
     * 
     * @param token the JWT token to check
     * @return true if token is blacklisted, false otherwise
     */
    boolean isBlacklisted(String token);

    /**
     * Removes expired tokens from the blacklist.
     * Should be called periodically via scheduled task to prevent memory leaks.
     */
    void removeExpiredTokens();

    /**
     * Gets the current size of the blacklist.
     * Useful for monitoring and metrics.
     * 
     * @return number of blacklisted tokens
     */
    int getBlacklistSize();

    /**
     * Clears all tokens from the blacklist.
     * Use with caution - only for administrative purposes.
     */
    void clearBlacklist();
}
