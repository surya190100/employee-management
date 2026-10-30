package com.surya.employee_management.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SecurityTestController {

    @GetMapping("/user/test")
    public Map<String, Object> userTest(Authentication authentication) {
        return Map.of(
                "username", authentication.getName(),
                "authorities", authentication.getAuthorities());
    }

    @GetMapping("/admin/test")
    public Map<String, Object> adminTest(Authentication authentication) {
        return Map.of(
                "username", authentication.getName(),
                "authorities", authentication.getAuthorities());
    }
}
