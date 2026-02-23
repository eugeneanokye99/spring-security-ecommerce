package com.shopjoy.config;

import com.shopjoy.entity.SecurityEventType;
import com.shopjoy.service.CustomUserDetailsService;
import com.shopjoy.service.SecurityAuditService;
import com.shopjoy.service.TokenBlacklistService;
import com.shopjoy.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter that validates JWT tokens on each request.
 * Extends OncePerRequestFilter to ensure the filter is executed once per request.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final SecurityAuditService securityAuditService;
    private final TokenBlacklistService tokenBlacklistService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String ipAddress = SecurityAuditService.extractClientIp(request);
        String userAgent = SecurityAuditService.extractUserAgent(request);

        String jwtToken = extractTokenFromRequest(request);
        
        if (jwtToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isTokenBlacklisted(jwtToken, ipAddress, userAgent)) {
            filterChain.doFilter(request, response);
            return;
        }

        authenticateUserFromToken(jwtToken, request, ipAddress, userAgent);
        
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts JWT token from Authorization header.
     *
     * @param request the HTTP request
     * @return the JWT token or null if not present
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        
        return authHeader.substring(BEARER_PREFIX.length());
    }

    /**
     * Checks if token is blacklisted and logs the attempt.
     *
     * @param token the JWT token
     * @param ipAddress the client IP address
     * @param userAgent the client user agent
     * @return true if blacklisted, false otherwise
     */
    private boolean isTokenBlacklisted(String token, String ipAddress, String userAgent) {
        if (tokenBlacklistService.isBlacklisted(token)) {
            log.debug("Token is blacklisted (user logged out)");
            securityAuditService.logEvent(
                null,
                SecurityEventType.ACCESS_DENIED,
                ipAddress,
                userAgent,
                "Attempted to use blacklisted token",
                false
            );
            return true;
        }
        return false;
    }

    /**
     * Validates token and authenticates user if valid.
     * Handles all token validation errors internally.
     *
     * @param token the JWT token
     * @param request the HTTP request
     * @param ipAddress the client IP address
     * @param userAgent the client user agent
     */
    private void authenticateUserFromToken(
            String token,
            HttpServletRequest request,
            String ipAddress,
            String userAgent
    ) {
        try {
            String username = jwtUtil.extractUsername(token);
            
            if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
                return;
            }

            // Check token expiry before DB call
            if (jwtUtil.isTokenExpired(token)) {
                log.warn("JWT token expired for request: {}", request.getRequestURI());
                securityAuditService.logEvent(
                    username,
                    SecurityEventType.TOKEN_EXPIRED,
                    ipAddress,
                    userAgent,
                    "JWT token expired: " + request.getRequestURI(),
                    false
                );
                return;
            }

            // Only load user from DB if token is valid
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (isUsernameMatching(token, userDetails)) {
                setAuthentication(userDetails, request);
                log.debug("JWT authentication successful for user: {}", username);
            } else {
                logTokenValidationFailure(username, ipAddress, userAgent);
            }

        } catch (Exception e) {
            SecurityEventType eventType = e instanceof ExpiredJwtException 
                    ? SecurityEventType.TOKEN_EXPIRED 
                    : SecurityEventType.TOKEN_INVALID;
            
            String username = e instanceof ExpiredJwtException 
                    ? ((ExpiredJwtException) e).getClaims().getSubject() 
                    : null;
            
            log.warn("JWT authentication failed: {}", e.getMessage());
            securityAuditService.logEvent(
                username,
                eventType,
                ipAddress,
                userAgent,
                "JWT authentication error: " + e.getMessage(),
                false
            );
        }
    }

    /**
     * Validates that the username in token matches the user details.
     *
     * @param token the JWT token
     * @param userDetails the user details
     * @return true if username matches, false otherwise
     */
    private boolean isUsernameMatching(String token, UserDetails userDetails) {
        String username = jwtUtil.extractUsername(token);
        return username.equals(userDetails.getUsername());
    }

    /**
     * Sets authentication in SecurityContext.
     *
     * @param userDetails the user details
     * @param request the HTTP request
     */
    private void setAuthentication(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    /**
     * Logs token validation failure.
     *
     * @param username the username
     * @param ipAddress the client IP address
     * @param userAgent the client user agent
     */
    private void logTokenValidationFailure(String username, String ipAddress, String userAgent) {
        log.warn("Invalid JWT token for user: {}", username);
        securityAuditService.logEvent(
            username,
            SecurityEventType.TOKEN_INVALID,
            ipAddress,
            userAgent,
            "Token validation failed",
            false
        );
    }
}
