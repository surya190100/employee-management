package com.surya.employee_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Security Test", description = "Endpoints for verifying role-based access control. Requires JWT Bearer authentication.")
@SecurityRequirement(name = "bearerAuth")
public class SecurityTestController {

    @GetMapping("/user/test")
    @Operation(
            summary = "User role test",
            description = "Accessible to authenticated users with ROLE_USER or ROLE_ADMIN."
    )
    public Map<String, Object> userTest(Authentication authentication) {
        return Map.of(
                "username", authentication.getName(),
                "authorities", authentication.getAuthorities());
    }

    @GetMapping("/admin/test")
    @Operation(
            summary = "Admin role test",
            description = "Accessible only to authenticated users with ROLE_ADMIN."
    )
    public Map<String, Object> adminTest(Authentication authentication) {
        return Map.of(
                "username", authentication.getName(),
                "authorities", authentication.getAuthorities());
    }
}
