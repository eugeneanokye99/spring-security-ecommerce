package com.shopjoy.aspect;

import com.shopjoy.exception.RateLimitExceededException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Aspect that logs rate limit violations for security monitoring.
 * Captures rate limit exceeded attempts for analysis and alerting.
 */
@Aspect
@Component
public class RateLimitAspect {
    
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    @AfterThrowing(
        pointcut = "execution(* com.shopjoy.service.AuthService.login(..)) || " +
                   "execution(* com.shopjoy.controller.AuthController.login(..))",
        throwing = "ex"
    )
    public void logRateLimitViolation(JoinPoint joinPoint, RateLimitExceededException ex) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        Object[] args = joinPoint.getArgs();
        
        String username = "UNKNOWN";
        if (args.length > 0 && args[0] != null) {
            String argString = args[0].toString();
            if (argString.contains("username")) {
                int startIndex = argString.indexOf("username=");
                if (startIndex != -1) {
                    startIndex += "username=".length();
                    int endIndex = argString.indexOf(",", startIndex);
                    if (endIndex == -1) {
                        endIndex = argString.indexOf(")", startIndex);
                    }
                    if (endIndex != -1) {
                        username = argString.substring(startIndex, endIndex).trim();
                    }
                }
            }
        }
        
        securityLogger.warn("[{}] RATE_LIMIT_EXCEEDED - Username: {}, Method: {}, " +
                "RetryAfter: {} seconds, Message: {}", 
                timestamp, 
                username,
                joinPoint.getSignature().getName(),
                ex.getRetryAfterSeconds(),
                ex.getMessage());
    }
}
