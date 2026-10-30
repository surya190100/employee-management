package com.surya.employee_management.controller;

import com.surya.employee_management.dto.AuthResponse;
import com.surya.employee_management.dto.LoginRequest;
import com.surya.employee_management.dto.RegisterRequest;
import com.surya.employee_management.dto.RegisterResponse;
import com.surya.employee_management.service.AuthService;
import com.surya.employee_management.service.JwtService;
import com.surya.employee_management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
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
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = userService.registerUser(request.getUsername(), request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authService.authenticate(request.getUsername(), request.getPassword());
        String token = jwtService.generateToken(authentication);
        long expiresInSeconds = jwtService.getExpirationSeconds();
        return ResponseEntity.ok(AuthResponse.of(token, expiresInSeconds));
    }
}
