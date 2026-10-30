package com.surya.employee_management.dto;

import lombok.Data;

@Data
public class RegisterResponse {

    private Long userId;
    private String username;
    private boolean enabled;
}
