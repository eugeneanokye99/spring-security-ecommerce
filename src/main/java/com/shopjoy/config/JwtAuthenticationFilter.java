package com.shopjoy.config;

import com.shopjoy.entity.SecurityEventType;
import com.shopjoy.service.CustomUserDetailsService;
import com.shopjoy.service.SecurityAuditService;
import com.shopjoy.service.TokenBlacklistService;
import com.shopjoy.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
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

        try {
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);

            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }

            String jwtToken = authHeader.substring(BEARER_PREFIX.length());
            
            if (tokenBlacklistService.isBlacklisted(jwtToken)) {
                log.debug("Token is blacklisted (user logged out)");
                securityAuditService.logEvent(
                    null,
                    SecurityEventType.ACCESS_DENIED,
                    request,
                    "Attempted to use blacklisted token",
                    false
                );
                filterChain.doFilter(request, response);
                return;
            }
            
            String username = null;

            try {
                username = jwtUtil.extractUsername(jwtToken);
            } catch (ExpiredJwtException e) {
                log.warn("JWT token expired for request: {}", request.getRequestURI());
                securityAuditService.logEvent(
                    e.getClaims().getSubject(),
                    SecurityEventType.TOKEN_EXPIRED,
                    request,
                    "JWT token expired: " + request.getRequestURI(),
                    false
                );
                filterChain.doFilter(request, response);
                return;
            } catch (JwtException e) {
                log.warn("Invalid JWT token: {}", e.getMessage());
                securityAuditService.logEvent(
                    null,
                    SecurityEventType.TOKEN_INVALID,
                    request,
                    "Invalid JWT token: " + e.getMessage(),
                    false
                );
                filterChain.doFilter(request, response);
                return;
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (isTokenValid(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("JWT authentication successful for user: {}", username);
                } else {
                    log.warn("Invalid JWT token for user: {}", username);
                    securityAuditService.logEvent(
                        username,
                        SecurityEventType.TOKEN_INVALID,
                        request,
                        "Token validation failed",
                        false
                    );
                }
            }

        } catch (Exception e) {
            log.error("JWT authentication failed: {}", e.getMessage());
            securityAuditService.logEvent(
                null,
                SecurityEventType.TOKEN_INVALID,
                request,
                "JWT authentication error: " + e.getMessage(),
                false
            );
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Validates the JWT token against the UserDetails.
     *
     * @param token       the JWT token
     * @param userDetails the user details
     * @return true if token is valid, false otherwise
     */
    private boolean isTokenValid(String token, UserDetails userDetails) {
        String username = jwtUtil.extractUsername(token);
        return username.equals(userDetails.getUsername()) && !jwtUtil.isTokenExpired(token);
    }
}
