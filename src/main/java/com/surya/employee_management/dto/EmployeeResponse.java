package com.surya.employee_management.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeResponse(
        Long employeeId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String department,
        String designation,
        BigDecimal salary,
        LocalDate dateOfJoining,
        boolean active) {
}
