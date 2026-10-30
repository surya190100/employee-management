package com.surya.employee_management.exception;

public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(Long employeeId) {
        super("Employee with ID " + employeeId + " was not found.");
    }
}
