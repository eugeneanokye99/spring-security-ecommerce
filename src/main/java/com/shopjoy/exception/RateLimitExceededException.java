package com.shopjoy.exception;

public class RateLimitExceededException extends BusinessException {
    
    private final long retryAfterSeconds;
    
    public RateLimitExceededException(String message, long retryAfterSeconds) {
        super(message, "RATE_LIMIT_EXCEEDED");
        this.retryAfterSeconds = retryAfterSeconds;
    }
    
    public RateLimitExceededException(long retryAfterSeconds) {
        super("Too many login attempts. Please try again later.", "RATE_LIMIT_EXCEEDED");
        this.retryAfterSeconds = retryAfterSeconds;
    }
    
    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
