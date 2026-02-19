package com.shopjoy.service.impl;

import com.shopjoy.dto.mapper.UserMapperStruct;
import com.shopjoy.dto.request.LoginRequest;
import com.shopjoy.dto.response.LoginResponse;
import com.shopjoy.entity.User;
import com.shopjoy.entity.UserType;
import com.shopjoy.repository.UserRepository;
import com.shopjoy.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthServiceImpl JWT authentication functionality.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplJwtTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapperStruct userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private LoginRequest loginRequest;
    private org.springframework.security.core.userdetails.User userDetails;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("$2a$10$hashedpassword")
                .firstName("Test")
                .lastName("User")
                .userType(UserType.CUSTOMER)
                .build();

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("TestPassword123");

        userDetails = new org.springframework.security.core.userdetails.User(
                "testuser",
                "$2a$10$hashedpassword",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
    }

    @Test
    void login_WithValidCredentials_ShouldReturnLoginResponseWithJwtToken() {
        // Arrange
        String expectedToken = "eyJhbGciOiJIUzI1NiJ9.test.token";
        Long expirationTime = 86400000L;

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn(expectedToken);
        when(jwtUtil.getExpirationTime()).thenReturn(expirationTime);

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(expectedToken, response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(expirationTime, response.getExpiresIn());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(userDetails);
    }

    @Test
    void login_WithAdminUser_ShouldReturnLoginResponseWithAdminRole() {
        // Arrange
        testUser.setUserType(UserType.ADMIN);
        String expectedToken = "eyJhbGciOiJIUzI1NiJ9.admin.token";
        Long expirationTime = 86400000L;

        org.springframework.security.core.userdetails.User adminUserDetails =
                new org.springframework.security.core.userdetails.User(
                        "testuser",
                        "$2a$10$hashedpassword",
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(adminUserDetails);
        when(jwtUtil.generateToken(adminUserDetails)).thenReturn(expectedToken);
        when(jwtUtil.getExpirationTime()).thenReturn(expirationTime);

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(expectedToken, response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(expirationTime, response.getExpiresIn());
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrowAuthenticationException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.core.AuthenticationException("Bad credentials") {});

        // Act & Assert
        assertThrows(com.shopjoy.exception.AuthenticationException.class,
                () -> authService.login(loginRequest));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, never()).generateToken(any());
    }
}
