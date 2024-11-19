package com.personal.inventory;

import com.personal.inventory.controller.AuthController;
import com.personal.inventory.entity.Role;
import com.personal.inventory.entity.User;
import com.personal.inventory.payload.request.LoginRequest;
import com.personal.inventory.payload.request.SignupRequest;
import com.personal.inventory.payload.response.JwtResponse;
import com.personal.inventory.payload.response.MessageResponse;
import com.personal.inventory.repository.RoleRepository;
import com.personal.inventory.repository.UserRepository;
import com.personal.inventory.security.jwt.JwtUtils;
import com.personal.inventory.service.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void authenticateUser_Success() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("john_doe");
        loginRequest.setPasswordHash("password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal())
                .thenReturn(new CustomUserDetails(1L, "john_doe", "john.doe@example.com", "password", Collections.emptyList()));
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("mocked-jwt-token");

        // Act
        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        // Assert
        JwtResponse jwtResponse = (JwtResponse) response.getBody();
        assert jwtResponse != null;
        assertEquals("mocked-jwt-token", jwtResponse.getToken());
        assertEquals("john_doe", jwtResponse.getUsername());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void registerUser_Success() {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("john_doe");
        signupRequest.setEmail("john.doe@example.com");
        signupRequest.setPasswordHash("password");
        signupRequest.setRoles(Collections.singleton("ROLE_USER"));

        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(new Role(1L, "ROLE_USER")));
        when(encoder.encode("password")).thenReturn("encoded-password");

        // Act
        ResponseEntity<?> response = authController.registerUser(signupRequest);

        // Assert
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assert messageResponse != null;
        assertEquals("User registered successfully!", messageResponse.getMessage());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_UsernameAlreadyExists() {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("john_doe");
        signupRequest.setEmail("john.doe@example.com");

        when(userRepository.existsByUsername("john_doe")).thenReturn(true);

        // Act
        ResponseEntity<?> response = authController.registerUser(signupRequest);

        // Assert
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assert messageResponse != null;
        assertEquals("Error: Username is already taken!", messageResponse.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_EmailAlreadyExists() {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("john_doe");
        signupRequest.setEmail("john.doe@example.com");

        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        // Act
        ResponseEntity<?> response = authController.registerUser(signupRequest);

        // Assert
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assert messageResponse != null;
        assertEquals("Error: Email is already in use!", messageResponse.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}
