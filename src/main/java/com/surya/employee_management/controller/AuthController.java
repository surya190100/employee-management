package com.surya.employee_management.controller;

import com.surya.employee_management.dto.AuthResponse;
import com.surya.employee_management.dto.LoginRequest;
import com.surya.employee_management.dto.RegisterRequest;
import com.surya.employee_management.dto.RegisterResponse;
import com.surya.employee_management.service.AuthService;
import com.surya.employee_management.service.JwtService;
import com.surya.employee_management.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints. No @SecurityRequirement — these are publicly accessible.
 * Swagger UI will not show a lock icon or require a Bearer token for these operations.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Public endpoints for user registration and login. No JWT token required.")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(UserService userService, AuthService authService, JwtService jwtService) {
        this.userService = userService;
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account. Returns the user ID, username, and enabled status. " +
                    "No authentication required."
    )
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = userService.registerUser(request.getUsername(), request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login and obtain JWT token",
            description = "Authenticates the user and returns a JWT access token. " +
                    "Use the returned 'accessToken' value in the Swagger UI Authorize dialog " +
                    "(click the lock icon at the top of the page). No prior authentication required."
    )
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authService.authenticate(request.getUsername(), request.getPassword());
        String token = jwtService.generateToken(authentication);
        long expiresInSeconds = jwtService.getExpirationSeconds();
        return ResponseEntity.ok(AuthResponse.of(token, expiresInSeconds));
    }
}
